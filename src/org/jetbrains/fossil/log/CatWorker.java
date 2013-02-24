package org.jetbrains.fossil.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;
import org.jetbrains.fossil.local.MoveWorker;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 4:36 PM
 */
public class CatWorker {
  private final Project myProject;

  public CatWorker(final Project project) {
    myProject = project;
  }

  public String cat(final File file, @Nullable final String revNum) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, MoveWorker.findParent(file), FCommandName.finfo);
    command.addParameters("-p");
    if (revNum != null && ! "HEAD".equals(revNum)) {
      command.addParameters("-r", revNum);
    }
    command.addParameters(file.getPath());
    return command.run();
  }
}
