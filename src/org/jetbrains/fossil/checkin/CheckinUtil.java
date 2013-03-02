package org.jetbrains.fossil.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.ui.UIUtil;
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
  public static final String BREAK_SEQUENCE = "contains CR/NL line endings; commit anyhow (yes/no/all)?";
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
      String result = null;
      for (int i = 0; i < 2; i++) {
        final FossilSimpleCommand command = new FossilSimpleCommand(myProject, parent, FCommandName.commit, BREAK_SEQUENCE);
        command.addParameters("-m", StringUtil.escapeStringCharacters(comment));
        for (File file : files) {
          command.addParameters(file.getPath());
        }
        result = command.run();
        if (result.contains(BREAK_SEQUENCE)) {
          final int ok[] = new int[1];
          UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
              ok[0] = Messages.showOkCancelDialog(myProject, "File(s) you are attempting to commit, contain CR/NL line endings;\n" +
                  "Fossil plugin needs to disable CR/NL check by changing crnl-glob setting to '*'.\n" +
                  "Do you wish to change crnl-glob setting and continue?", "CR/NL line endings", Messages.getQuestionIcon());
            }
          });
          if (ok[0] == Messages.OK) {
            final FossilSimpleCommand settingsCommand = new FossilSimpleCommand(myProject, parent, FCommandName.settings);
            settingsCommand.addParameters("crnl-glob", "*");
            settingsCommand.run();
            continue;
          }
        }
        break;
      }
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
