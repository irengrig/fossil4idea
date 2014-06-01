package org.github.irengrig.fossil4idea.local;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.util.Consumer;
import com.yourkit.util.FileUtil;
import org.github.irengrig.fossil4idea.checkin.AddUtil;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilLineCommand;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.log.CommitWorker;
import org.github.irengrig.fossil4idea.repository.FossilContentRevision;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.repository.FossilRevisionNumber;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 8:44 PM
 */
public class LocalUtil {
  public static void reportChanges(final Project project, final File directory,
                                   final ChangelistBuilder clb) throws VcsException {
    final LineParser lineParser = new LineParser(project, clb, directory);
    final StringBuilder err = new StringBuilder();
    askChanges(project, directory, lineParser, err);
    if (lineParser.hasSomethingForDiff()) {
      askDiff(project, directory, lineParser, err);
      final List<Change> changes = lineParser.getDiffedChanges();
      for (Change change : changes) {
        clb.processChange(change, FossilVcs.getVcsKey());
      }
    }
    if (err.length() > 0) {
      throw new FossilException(err.toString());
    }
  }

  private static void askDiff(Project project, File directory, final LineParser lineParser, final StringBuilder err) {
    final FossilLineCommand command = new FossilLineCommand(project, directory, FCommandName.diff);
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(String s, Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          try {
            lineParser.parseDiffLine(s);
          } catch (FossilException e) {
            err.append(e.getMessage());
          }
        } else if (ProcessOutputTypes.STDERR.equals(key)) {
          err.append(s).append('\n');
        }
      }

      @Override
      public void processTerminated(int i) {
      }

      @Override
      public void startFailed(Throwable throwable) {
      }
    });
  }

  private static void askChanges(Project project, File directory, final LineParser lineParser, final StringBuilder err) {
    final FossilLineCommand command = new FossilLineCommand(project, directory, FCommandName.changes);
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(final String s, final Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          try {
            lineParser.parseChangesLine(s);
          } catch (FossilException e) {
            err.append(e.getMessage());
          }
        } else if (ProcessOutputTypes.STDERR.equals(key)) {
          err.append(s).append('\n');
        }
      }

      @Override
      public void processTerminated(final int i) {
      }

      @Override
      public void startFailed(final Throwable throwable) {
      }
    });
  }

  public static void reportUnversioned(final Project project, final File directory, final Consumer<File> consumer) throws FossilException {
    final FossilLineCommand command = new FossilLineCommand(project, directory, FCommandName.extras);
    command.addParameters("--dotfiles");
    final StringBuilder err = new StringBuilder();
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(final String s, final Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          final String line = s.trim();
          consumer.consume(new File(directory, line));
        } else if (ProcessOutputTypes.STDERR.equals(key)) {
          err.append(s).append('\n');
        }
      }

      @Override
      public void processTerminated(final int exitCode) {
      }

      @Override
      public void startFailed(final Throwable exception) {
      }
    });
    if (err.length() > 0) {
      throw new FossilException(err.toString());
    }
  }

  private static class LineParser {
    public static final String INDEX = "Index: ";
    private final Project myProject;
    private final ChangelistBuilder myClb;
    private final File myBase;
    // for diff
    private final Set<String> myPathsForDiff;
    private boolean myInsideDiff;
    private int myLastDiffHeaderLine;
    private final Map<String, StringBuilder> myPatches;
    private String myPreviousPatchName;
    private String myCurrentFile;
    private StringBuilder myBuff;

    public LineParser(Project myProject, ChangelistBuilder myClb, File myBase) {
      this.myProject = myProject;
      this.myClb = myClb;
      this.myBase = myBase;
      myPathsForDiff = new HashSet<String>();
      myPatches = new HashMap<String, StringBuilder>();
      myBuff = new StringBuilder();
    }

    public void parseChangesLine(final String s) throws FossilException {
      final String line = s.trim();
      final int spaceIdx = line.indexOf(' ');
      if (spaceIdx == -1) throw new FossilException("Can not parse status line: '" + s + "'");
      final String typeName = line.substring(0, spaceIdx);
      final FileStatus type = ourOneSideTypes.get(typeName);
      final File file = new File(myBase, line.substring(spaceIdx).trim());
      if (type != null) {
        myClb.processChange(createChange(myProject, file, type), FossilVcs.getVcsKey());
        return;
      }
      if ("MISSING".equals(typeName)) {
        myClb.processLocallyDeletedFile(VcsContextFactory.SERVICE.getInstance().createFilePathOnDeleted(file, false));
        return;
      }
      if ("CONFLICT".equals(typeName)) {
        myClb.processChange(createChange(myProject, file, FileStatus.MERGED_WITH_CONFLICTS), FossilVcs.getVcsKey());
        return;
      }
      if (ourWithDiffTypes.contains(typeName)) {
        myPathsForDiff.add(s);
      }
      // suppress for now
//      throw new FossilException("Can not parse status line: '" + s + "'");
    }

    public boolean hasSomethingForDiff() {
      return ! myPathsForDiff.isEmpty();
    }

    public void parseDiffLine(String s) throws FossilException {
      if (myInsideDiff && myLastDiffHeaderLine >= 0) {
        boolean isNext = false;
        if (myLastDiffHeaderLine == 0) {
          isNext = s.startsWith("==================");
        } else if (myLastDiffHeaderLine == 1) {
          isNext = s.startsWith("--- ") && s.length() > 4;
        } else if (myLastDiffHeaderLine == 2) {
          isNext = s.startsWith("+++ ") && s.length() > 4;
        }
        if (isNext) {
          myBuff.append(s).append("\n");
          ++ myLastDiffHeaderLine;
          if (myLastDiffHeaderLine == 3) {
            // end of header
            myLastDiffHeaderLine = -1;
            myPatches.get(myCurrentFile).append(myBuff);
            myBuff.setLength(0);
          }
        } else {
          // it all was just part of previous patch!
          if (myPreviousPatchName == null || myPatches.get(myPreviousPatchName) == null) throw new FossilException("Can not parse patch - no header");
          final StringBuilder stringBuilder = myPatches.get(myPreviousPatchName);
          stringBuilder.append(myBuff);
          myBuff.setLength(0);
          myLastDiffHeaderLine = -1;
        }
        return;
      }
      if (s.startsWith(INDEX)) {
        myInsideDiff = true;
        myLastDiffHeaderLine = 0;
        myPreviousPatchName = myCurrentFile;
        myCurrentFile = s.substring(INDEX.length()).trim();
        final StringBuilder sb = new StringBuilder();
        myPatches.put(myCurrentFile, sb);
        sb.append(s);
        return;
      }
      if (s.startsWith("ADDED") || s.startsWith("DELETED")) {
        // skip;
        myInsideDiff = false;
        myLastDiffHeaderLine = -1;
        return;
      }
      if (myInsideDiff) {
        myPatches.get(myCurrentFile).append(s).append("\n");
      }
      // what is it if...?
    }

    public List<Change> getDiffedChanges() throws VcsException {
      final List<Change> result = new ArrayList<Change>();
      for (Map.Entry<String, StringBuilder> e : myPatches.entrySet()) {
        final File file = new File(myBase, e.getKey().trim());
        final ContentRevision after = createAfter(file, FileStatus.MODIFIED);
        final String newContent = new DiffUtil().execute(after.getContent(), e.getValue().toString(), file.getName());
        final ContentRevision before = new SimpleContentRevision(newContent, new FilePathImpl(file, false), "Local");
        result.add(new Change(before, after));
      }
      return result;
    }
  }

  private static final Set<String> ourWithDiffTypes = new HashSet<String>();
  static {
    ourWithDiffTypes.add("EDITED");
    ourWithDiffTypes.add("RENAMED");
  }

  private static final Map<String, FileStatus> ourOneSideTypes = new HashMap<String, FileStatus>(7);
  static {
    ourOneSideTypes.put("ADDED", FileStatus.ADDED);
    ourOneSideTypes.put("DELETED", FileStatus.DELETED);
  }

  public static Change createChange(final Project project, final File file, final FileStatus changeTypeEnum) throws FossilException {
    return new Change(createBefore(project, file, changeTypeEnum), createAfter(file, changeTypeEnum));
  }

  private static ContentRevision createBefore(final Project project, final File file, final FileStatus changeTypeEnum) throws FossilException {
    if (FileStatus.ADDED.equals(changeTypeEnum)) {
      return null;
    }
    try {
      return new FossilContentRevision(project, createFilePath(file), new CommitWorker(project).getBaseRevisionNumber(file));
    } catch (VcsException e) {
      if (e instanceof FossilException) throw (FossilException) e;
      throw new FossilException(e);
    }
  }

  private static ContentRevision createAfter(final File file, final FileStatus changeTypeEnum) {
    if (FileStatus.DELETED.equals(changeTypeEnum)) {
      return null;
    }
    final FilePath filePath = createFilePath(file);
    if (filePath.getFileType() != null && ! filePath.isDirectory() && filePath.getFileType().isBinary()) {
      return new CurrentBinaryContentRevision(filePath);
    }
    return new CurrentContentRevision(filePath);
  }

  // seems that folders are not versioned
  private static FilePath createFilePath(final File file) {
    if (! file.exists()) {
      return VcsContextFactory.SERVICE.getInstance().createFilePathOnDeleted(file, false);
    }
    return VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
  }

  public static void rollbackChanges(final Project project, final List<Change> changes, final RollbackProgressListener listener) throws VcsException {
    final List<File> files = ChangesUtil.getIoFilesFromChanges(changes);
    rollbackFiles(project, listener, files);
  }

  private static void rollbackFiles(final Project project, final RollbackProgressListener listener, final List<File> files) throws VcsException {
    final File parent = AddUtil.tryFindCommonParent(project, files);
    if (parent != null) {
      final FossilSimpleCommand command = new FossilSimpleCommand(project, parent, FCommandName.revert);
      for (File file : files) {
        command.addParameters(file.getPath());
      }
      command.run();
    } else {
      for (File file : files) {
        final FossilSimpleCommand command = new FossilSimpleCommand(project, MoveWorker.findParent(file), FCommandName.revert);
        command.addParameters(file.getPath());
        command.run();
        listener.accept(file);
      }
    }
  }

  public static void rollbackLocallyDeletedChanges(final Project project, final List<FilePath> files, final RollbackProgressListener listener) throws VcsException {
    rollbackFiles(project, listener, ObjectsConvertor.fp2jiof(files));
  }
}
