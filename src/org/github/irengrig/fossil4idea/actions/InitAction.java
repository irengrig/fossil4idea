package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.github.irengrig.fossil4idea.checkout.CheckoutUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Irina.Chernushina on 5/29/2014.
 */
public class InitAction extends com.intellij.openapi.actionSystem.AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
    if (project == null) return;

    final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(new FileSaverDescriptor("Init Fossil Repository",
            "Select file where to create new Fossil repository."), project);
    final VirtualFileWrapper wrapper = dialog.save(null, null);
    if (wrapper == null) return;
    final Task.Backgroundable task = new Task.Backgroundable(project, "Open Fossil Repository", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
      @Override
      public void run(@NotNull ProgressIndicator progressIndicator) {
        try {
          new CheckoutUtil(project).initRepository(wrapper.getFile());
          VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil repository successfully created: " + wrapper.getFile().getPath(), MessageType.INFO);
        } catch (VcsException e) {
          VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil repository not created: " + e.getMessage(), MessageType.ERROR);
        }
      }
    };
    ProgressManager.getInstance().run(task);
  }
}
