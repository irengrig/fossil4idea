package org.jetbrains.fossil.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;
import org.jetbrains.fossil.local.MoveWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 2:59 PM
 */
public class CheckinUtil {
  private final Project myProject;

  public CheckinUtil(final Project project) {
    myProject = project;
  }

  /**
   * @return list of committed revisions hashes
   */
  public List<String> checkin(final List<File> files, final String comment) throws VcsException {
    final File parent = AddUtil.tryFindCommonParent(myProject, files);
    if (parent != null) {
      final FossilSimpleCommand command = new FossilSimpleCommand(myProject, parent, FCommandName.commit);
      command.addParameters("-m", "\"" + StringUtil.escapeStringCharacters(comment) + "\"");
      for (File file : files) {
        command.addParameters(file.getPath());
      }
      final String result = command.run();
      return Collections.singletonList(parseHash(result));
    } else {
      final List<String> hashes = new ArrayList<String>();
      for (File file : files) {
        final FossilSimpleCommand command = new FossilSimpleCommand(myProject, MoveWorker.findParent(file), FCommandName.commit);
        command.addParameters("-m", "\"" + StringUtil.escapeStringCharacters(comment) + "\"");
        command.addParameters(file.getPath());
        hashes.add(parseHash(command.run()));
      }
      return hashes;
    }
  }

  private String parseHash(String result) throws FossilException {
    if (result == null) throw new FossilException("Can not parse 'commit' result: null");
    result = result.trim();
    final String prefix = "New_Version: ";
    if (! result.startsWith(prefix)) {
      throw new FossilException("Can not parse 'commit' result: " + result);
    }
    return result.substring(prefix.length()).trim();
  }
}
