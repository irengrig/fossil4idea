package org.jetbrains.fossil.init;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 11:39 PM
 */
public class CreateUtil {
  private final Project myProject;
  private final File myRepoPath;

  private String myUserName;
  private String myPassword;
  private String myProjectId;
  private String myServerId;

  public CreateUtil(final Project project, final String repoPath) throws FossilException {
    myProject = project;
    myRepoPath = new File(repoPath);
    if (myRepoPath.exists()) {
      throw new FossilException("Can not create repository at " + repoPath + ".\nFile already exists.");
    }
    if (! myRepoPath.getParentFile().exists()) {
      throw new FossilException("Can not create repository at " + repoPath + ".\nParent directory does not exist.");
    }
  }

  /*project-id: hash
  server-id:  hash
  admin-user: ___ (initial password is "__")*/
  public void createRepository() throws VcsException {
    final FossilSimpleCommand fossilCommand = new FossilSimpleCommand(myProject, myRepoPath.getParentFile(), FCommandName.new_);
    fossilCommand.addParameters(myRepoPath.getPath());
    String result = fossilCommand.run();
    result = result.replace("\r", "");
    final String[] split = result.split("\n");
    final List<String> lines = new ArrayList<String>(3);
    for (String line : split) {
      if (! StringUtil.isEmptyOrSpaces(line)) {
        lines.add(line.trim());
      }
    }

    if (lines.size() != 3) {
      throw new FossilException("Can not parse 'new' output: '" + result + "'");
    }
    final String[] expectedHeaders = {"project-id:", "server-id:", "admin-user:"};
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (! line.startsWith(expectedHeaders[i])) {
        throw new FossilException("Can not parse 'new' output, line #" + (i + 1) + ": '" + result + "'");
      }
    }
    myProjectId = new String(lines.get(0).substring(expectedHeaders[0].length() + 1));
    myServerId = new String(lines.get(1).substring(expectedHeaders[1].length()) + 1);
    final String userPswd = lines.get(2).substring(expectedHeaders[2].length()).trim();
    final int idxSpace = userPswd.indexOf(' ');
    if (idxSpace == -1) {
      throw new FossilException("Can not parse 'new' output, user-password area: '" + result + "'");
    }
    myUserName = new String(userPswd.substring(0, idxSpace));
    final int quot1 = userPswd.indexOf('"', idxSpace + 1);
    int quot2 = -1;
    if (quot1 >= 0) {
      quot2 = userPswd.indexOf('"', quot1 + 1);
    }
    if (quot1 == -1 || quot2 == -1) {
      throw new FossilException("Can not parse 'new' output, user-password area: '" + result + "'");
    }
    myPassword = userPswd.substring(quot1 + 1 , quot2);
  }

  public void openRepoTo(final File where, final File repo) throws VcsException {
    if (where.exists() && ! where.isDirectory()) {
      throw new FossilException("Can not checkout to " + where.getPath() + ". File already exists.");
    }
    if (! repo.exists() || repo.isDirectory()) {
      throw new FossilException("Can not checkout from " + repo.getPath());
    }
    if (! where.exists()) {
      where.mkdirs();
    }
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, where, FCommandName.open);
    //command.addParameters("--latest");
    command.addParameters(repo.getPath());
    try {
    final String result = command.run();
    } catch (VcsException e) {
      e.printStackTrace();
    }
  }

  public String getUserName() {
    return myUserName;
  }

  public String getPassword() {
    return myPassword;
  }

  public String getProjectId() {
    return myProjectId;
  }

  public String getServerId() {
    return myServerId;
  }
}
