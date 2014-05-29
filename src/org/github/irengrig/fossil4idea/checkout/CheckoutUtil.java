package org.github.irengrig.fossil4idea.checkout;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;

import java.io.File;

/**
 * Created by Irina.Chernushina on 1/30/14.
 */
public class CheckoutUtil {
  private final Project myProject;

  public CheckoutUtil(final Project project) {
    myProject = project;
  }

  public void cloneRepo(final String url, final String localPath) throws VcsException {
    final File file = new File(localPath);
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, file.getParentFile(), FCommandName.clone);
    command.addParameters(url, localPath);
    final String result = command.run();
  }

  public void checkout(final File repo, final File target, final String hash) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, target, FCommandName.open);
    command.addParameters(repo.getAbsolutePath());
    if (hash != null) {
      command.addParameters(hash);
    }
    command.run();
  }

  public void initRepository(final File repo) throws VcsException {
    final File parentFile = repo.getParentFile();
    if (parentFile.exists() && ! parentFile.isDirectory()) {
      throw new VcsException("Can not create Fossil repository, " + parentFile.getPath() + " is not a directory.");
    }
    if (! parentFile.exists() && ! parentFile.mkdirs()) {
      throw new VcsException("Can not create Fossil repository, can not create parent directory: " + parentFile.getPath());
    }
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, parentFile, FCommandName.init);
    command.addParameters(repo.getName());
    command.run();
  }
}
