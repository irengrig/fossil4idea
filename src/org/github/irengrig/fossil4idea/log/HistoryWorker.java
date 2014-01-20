package org.github.irengrig.fossil4idea.log;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.LineProcessEventListener;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilLineCommand;
import org.github.irengrig.fossil4idea.local.MoveWorker;
import org.jetbrains.annotations.Nullable;
import org.github.irengrig.fossil4idea.FossilException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 7:06 PM
 */
public class HistoryWorker {
  private final Project myProject;
  private final StringBuilder mySb;
  private boolean myOk;
  private boolean myFirstLine;
  private FilePath myPath;

  public HistoryWorker(final Project project) {
    myProject = project;
    mySb = new StringBuilder();
    myFirstLine = true;
  }

  public void report(final FilePath path, final VcsAppendableHistorySessionPartner partner) throws VcsException {
    myPath = path;
    final File ioFile = path.getIOFile();
    final FossilLineCommand command = new FossilLineCommand(myProject, MoveWorker.findParent(ioFile), FCommandName.finfo);
    command.addParameters(path.getPath());
    final MySession session = new MySession(myProject, ioFile, new ArrayList<VcsFileRevision>(),
        new CommitWorker(myProject).getBaseRevisionNumber(ioFile));
    partner.reportCreatedEmptySession(session);
    final StringBuilder err = new StringBuilder();
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(final String line, final Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          try {
            parseLine(line.trim(), partner);
          } catch (VcsException e) {
            err.append(e.getMessage());
          }
        } else if (ProcessOutputTypes.STDERR.equals(key)) {
          err.append(line).append('\n');
        }
      }

      @Override
      public void processTerminated(final int exitCode) {
        partner.finished();
      }

      @Override
      public void startFailed(final Throwable exception) {
        partner.finished();
      }
    });
    if (err.length() > 0) {
      throw new FossilException(err.toString());
    }
    if (! myOk) {
      throw new FossilException(mySb.toString());
    }
  }

  /*History of a/aabb.txt
  2013-02-24 [8191f07a6d] "one more" (user: Irina.Chernushina, artifact:
             [f619008895], branch: trunk)
  2013-02-24 [dd554bf674] test commit2 (user: Irina.Chernushina, artifact:
             [7581507ad6], branch: trunk)
  2013-02-24 [95ca278a89] test commit (user: Irina.Chernushina, artifact:
             [da39a3ee5e], branch: trunk)*/
  private void parseLine(final String line, final VcsAppendableHistorySessionPartner partner) throws VcsException {
    if (myFirstLine) {
      if (line.startsWith("History of")) {
        myOk = true;
        return;
      } else {
        mySb.append(line);
      }
      myFirstLine = false;
      return;
    }
    if (! myOk) {
      mySb.append(line);
      return;
    }

    if (mySb.length() > 0) mySb.append(' ');
    mySb.append(line);
    if (')' == mySb.charAt(mySb.length() - 1)) {
      // line complete
      final String totalline = mySb.toString();
      mySb.setLength(0);
      doParseLine(totalline, partner);
    }
  }

  private void doParseLine(final String totalline, final VcsAppendableHistorySessionPartner partner) throws VcsException {
    final int idxSq1 = totalline.indexOf('[');
    final int idxSq2 = totalline.indexOf(']');
    if (idxSq1 == -1 || idxSq2 == -1) throw new FossilException("Can not parse history line: " + totalline);
    final String revNum = new String(totalline.substring(idxSq1 + 1, idxSq2));
    final String left = totalline.substring(idxSq2 + 1);
    final int idxRound = left.indexOf('(');
    if (idxRound == -1) throw new FossilException("Can not parse history line: " + totalline);
    // first symbol is space, amd space before (
    final String comment = left.substring(1, idxRound - 1);
    final String userPattern = "user: ";
    if (! userPattern.equals(left.substring(idxRound + 1, idxRound + 1 + userPattern.length())))
      throw new FossilException("Can not parse history line (user:): " + totalline);
    final int idxNameEnd = left.indexOf(", artifact:", idxRound + 1 + userPattern.length());
    if (idxNameEnd == -1) throw new FossilException("Can not parse history line (name end): " + totalline);
    final String name = left.substring(idxRound + 1 + userPattern.length(), idxNameEnd);
    partner.acceptRevision(new FossilFileRevision(myProject, myPath,
        new CommitWorker(myProject).getRevisionNumber(myPath.getIOFile(), revNum), name, comment));
  }

  private static class MySession extends VcsAbstractHistorySession {
    private final Project myProject;
    private final File myFile;
    private static final Logger LOG = Logger.getInstance("#org.jetbrains.fossil4idea.log.HistoryWorker.MySession");

    private MySession(final Project project, final File file, final List<? extends VcsFileRevision> revisions) {
      super(revisions);
      myProject = project;
      myFile = file;
    }

    private MySession(final Project project, final File file, final List<? extends VcsFileRevision> revisions,
                      final VcsRevisionNumber currentRevisionNumber) {
      super(revisions, currentRevisionNumber);
      myProject = project;
      myFile = file;
    }

    @Nullable
    @Override
    protected VcsRevisionNumber calcCurrentRevisionNumber() {
      try {
        return new CommitWorker(myProject).getBaseRevisionNumber(myFile);
      } catch (VcsException e) {
        LOG.info(e);
        return null;
      }
    }

    @Override
    public VcsHistorySession copy() {
      return new MySession(myProject, myFile, getRevisionList(), getCurrentRevisionNumber());
    }

    @Nullable
    @Override
    public HistoryAsTreeProvider getHistoryAsTreeProvider() {
      return null;
    }
  }
}
