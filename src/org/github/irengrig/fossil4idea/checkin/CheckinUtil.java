package org.github.irengrig.fossil4idea.checkin;

import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsQuickListPopupAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import org.github.irengrig.fossil4idea.FossilConfigurable;
import org.github.irengrig.fossil4idea.FossilConfiguration;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.local.MoveWorker;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.pull.FossilUpdateConfigurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 2:59 PM
 */
public class CheckinUtil {
  public static final String QUESTION = "Commit anyhow (a=all/c=convert/y/N)?";
  public static final String BREAK_SEQUENCE = "contains CR/NL line endings";
  public static final String PUSH_TO = "Push to";
  private final Project myProject;
  public static final String PREFIX = "New_Version: ";

  public CheckinUtil(final Project project) {
    myProject = project;
  }

  public void push() {
    final FossilVcs fossil = FossilVcs.getInstance(myProject);
    final VirtualFile[] roots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(fossil);
    if (roots.length == 0) {
//      PopupUtil.showBalloonForActiveComponent("Error occurred while pushing: No roots under Fossil found.", MessageType.ERROR);
      return;
    }

    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        final List<FilePath> filePaths = ObjectsConvertor.vf2fp(Arrays.asList(roots));
        final FossilUpdateConfigurable configurable = (FossilUpdateConfigurable) fossil.getUpdateEnvironment().createConfigurable(filePaths);
        final JComponent component = configurable.createComponent();
        final DialogBuilder builder = new DialogBuilder(myProject);
        builder.setCenterPanel(component);
        builder.addOkAction();
        builder.addCancelAction();
        builder.setDimensionServiceKey(getClass().getName());
        builder.setTitle("Push into Fossil Repository");
        builder.setOkOperation(new Runnable() {
          @Override
          public void run() {
            builder.getWindow().setVisible(false);
            try {
              configurable.apply();
              final ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
              if (pi != null) {
                pi.setText("Pushing...");
              }
              ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
                @Override
                public void run() {
                  try {
                    final String s = pushImpl();
                    PopupUtil.showBalloonForActiveComponent(s, MessageType.INFO);
                  } catch (VcsException e) {
                    // todo as notification
                    PopupUtil.showBalloonForActiveComponent("Error occurred while pushing: " + e.getMessage(), MessageType.ERROR);
                  }
                }
              });
            } catch (ConfigurationException e) {
              PopupUtil.showBalloonForActiveComponent("Error occurred while pushing: " + e.getMessage(), MessageType.ERROR);
            }
          }
        });
//        builder.setPreferredFocusComponent(configurable.);
        builder.show();
      }
    });
  }

  private String pushImpl() throws VcsException {
    final StringBuilder sb = new StringBuilder();
    final FossilConfiguration instance = FossilConfiguration.getInstance(myProject);
    final Map<File, String> remoteUrls = instance.getRemoteUrls();

    final FossilVcs fossil = FossilVcs.getInstance(myProject);
    final VirtualFile[] roots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(fossil);
    if (roots.length == 0) throw new FossilException("No roots under Fossil found.");

    for (VirtualFile root : roots) {
      final File file = new File(root.getPath());
      final String remote = remoteUrls.get(file);
      String s = pushOneRoot(file, remote);
      s = s.isEmpty() && remote != null ? "Pushed to " + remote : s;
      if (! s.isEmpty()) {
        if (sb.length() > 0) sb.append("\n");
        sb.append(s);
      }
    }
    return sb.toString();
  }

  private String pushOneRoot(final File file, @Nullable final String url) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, file, FCommandName.push, BREAK_SEQUENCE);
    if (url != null) {
      command.addParameters(url);
    }
    final String run = command.run();
    final String[] split = run.split("\n");
    for (String s : split) {
      if (s.startsWith("Error: ")) {
        throw new FossilException(s);
      }
      if (s.startsWith(PUSH_TO)) {
        return s.substring(PUSH_TO.length());
      }
    }
    /*Push to file://D:/testprojects/_fc_/r/repo_1*/
    return "";
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
        command.addBreakSequence("fossil knows nothing about");
        command.addBreakSequence(QUESTION);
        command.addBreakSequence("Autosync failed");
        command.addSkipError("Abandoning commit due to CR/NL line endings");
        command.addParameters("-m", StringUtil.escapeStringCharacters(comment));
        for (File file : files) {
          final String relative = FileUtil.getRelativePath(parent, file);
          command.addParameters(relative);
        }
        result = command.run();
        if (result.contains(QUESTION) && result.contains(BREAK_SEQUENCE)) {
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
            settingsCommand.addParameters("crnl-glob", "'*'");
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
    final String[] split = result.split("\n");
    for (int i = 0; i < split.length; i++) {
      final String s = split[i];
      if (s.startsWith(PREFIX)) return s.substring(PREFIX.length()).trim();
    }
    throw new FossilException("Can not parse 'commit' result: " + result);
  }
}
