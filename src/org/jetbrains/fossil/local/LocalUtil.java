package org.jetbrains.fossil.local;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.LineProcessEventListener;
import com.intellij.openapi.vcs.changes.dbCommitted.ChangeTypeEnum;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilLineCommand;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 8:44 PM
 */
public class LocalUtil {
  public static void reportChanges(final Project project, final File directory,
                                   final PairConsumer<File, FileStatus> consumer) throws FossilException {
    final FossilLineCommand command = new FossilLineCommand(project, directory, FCommandName.changes);
    final StringBuilder err = new StringBuilder();
    command.startAndWait(new LineProcessEventListener() {
      @Override
      public void onLineAvailable(final String s, final Key key) {
        if (ProcessOutputTypes.STDOUT.equals(key)) {
          try {
            parseChangesLine(directory, s, consumer);
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

  private static void parseChangesLine(final File base, final String s, final PairConsumer<File, FileStatus> consumer) throws FossilException {
    final String line = s.trim();
    final int spaceIdx = line.indexOf(' ');
    if (spaceIdx == -1) throw new FossilException("Can not parse status line: '" + s + "'");
    final FileStatus type = myLocalTypes.get(line.substring(0, spaceIdx));
    if (type == null) {
      throw new FossilException("Can not parse status line: '" + s + "'");
    }
    consumer.consume(new File(base, line.substring(spaceIdx).trim()), type);
  }

  private static final Map<String, FileStatus> myLocalTypes = new HashMap<String, FileStatus>(7);
  static {
    myLocalTypes.put("EDITED", FileStatus.MODIFIED);
    myLocalTypes.put("ADDED", FileStatus.ADDED);
    myLocalTypes.put("DELETED", FileStatus.DELETED);
    myLocalTypes.put("MISSING", FileStatus.DELETED_FROM_FS);
    // todo more?
  }
}
