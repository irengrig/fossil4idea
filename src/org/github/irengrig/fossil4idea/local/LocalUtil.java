package org.github.irengrig.fossil4idea.local;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.util.Consumer;
import org.github.irengrig.fossil4idea.checkin.AddUtil;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilLineCommand;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.log.CommitWorker;
import org.github.irengrig.fossil4idea.repository.FossilContentRevision;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.FossilVcs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 8:44 PM
 */
public class LocalUtil {
  public static void reportChanges(final Project project, final File directory,
                                   final ChangelistBuilder clb) throws FossilException {
    final FossilLineCommand command = new FossilLineCommand(project, directory, FCommandName.changes);
    final StringBuilder err = new StringBuilder();
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(final String s, final Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          try {
            parseChangesLine(project, directory, s, clb);
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
    if (err.length() > 0) {
      throw new FossilException(err.toString());
    }
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

  private static void parseChangesLine(final Project project, final File base, final String s, final ChangelistBuilder clb) throws FossilException {
    final String line = s.trim();
    final int spaceIdx = line.indexOf(' ');
    if (spaceIdx == -1) throw new FossilException("Can not parse status line: '" + s + "'");
    final String typeName = line.substring(0, spaceIdx);
    final FileStatus type = myLocalTypes.get(typeName);
    final File file = new File(base, line.substring(spaceIdx).trim());
    if (type != null) {
      clb.processChange(createChange(project, file, type), FossilVcs.getVcsKey());
    } else if ("MISSING".equals(typeName)) {
      clb.processLocallyDeletedFile(VcsContextFactory.SERVICE.getInstance().createFilePathOnDeleted(file, false));
    } else if ("RENAMED".equals(typeName)) {
      clb.processChange(new RenamedChange(createAfter(file, FileStatus.MODIFIED)), FossilVcs.getVcsKey());
    } else {
      throw new FossilException("Can not parse status line: '" + s + "'");
    }
  }

  private static final Map<String, FileStatus> myLocalTypes = new HashMap<String, FileStatus>(7);
  static {
    myLocalTypes.put("EDITED", FileStatus.MODIFIED);
    myLocalTypes.put("ADDED", FileStatus.ADDED);
    myLocalTypes.put("DELETED", FileStatus.DELETED);
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
