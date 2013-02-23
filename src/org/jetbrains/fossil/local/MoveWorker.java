package org.jetbrains.fossil.local;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 10:49 PM
 */
public class MoveWorker {
  private final Project myProject;

  public MoveWorker(final Project project) {
    myProject = project;
  }

  public void doRename(final File oldPath, final File newPath) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, findParent(oldPath), FCommandName.rename);
    command.addParameters(newPath.getName());
    command.run();
  }

  public void doMove(final File oldPath, final File targetDir) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, findParent(oldPath), FCommandName.rename);
    command.addParameters(targetDir.getPath());
    command.run();
  }

  public static File findParent(final File oldPath) throws FossilException {
    File current = oldPath;
    while (current != null) {
      if (current.exists() && current.isDirectory()) return current;
      current = current.getParentFile();
    }
    throw new FossilException("Can not find existing parent directory for file: " + oldPath.getPath());
  }
}
