package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.checkout.CheckoutUtil;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.ui.UiManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created by Irina.Chernushina on 5/29/2014.
 */
public class RunUIAction extends AnAction {
  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) return;
    final UiManager uiManager = FossilVcs.getInstance(project).getUiManager();
    if (uiManager.isRun()) {
      e.getPresentation().setText("Stop Web UI server");
    } else {
      e.getPresentation().setText("Run Web UI");
    }
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
    if (project == null) return;
    final UiManager uiManager = FossilVcs.getInstance(project).getUiManager();
    if (uiManager.isRun()) {
      uiManager.stop();
    } else {
      uiManager.run();
    }
  }
}
