package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import com.intellij.util.Consumer;
import org.github.irengrig.fossil4idea.checkout.CheckoutUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class OpenAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
    if (project == null) return;

    final CheckoutUIWorker uiWorker = new CheckoutUIWorker();
    uiWorker.showDialog(project, new Runnable() {
      @Override
      public void run() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Open Fossil Repository", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
          @Override
          public void run(@NotNull ProgressIndicator progressIndicator) {
            try {
              new CheckoutUtil(project).checkout(new File(uiWorker.getRepo()), new File(uiWorker.getLocalPath()), null);
              VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil repository successfully opened: " + uiWorker.getLocalPath(), MessageType.INFO);
            } catch (VcsException e) {
              VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil repository not opened: " + e.getMessage(), MessageType.ERROR);
            }
          }
        });
      }
    });
  }

  private static class CheckoutUIWorker {
    private TextFieldWithBrowseButton myLocalPath;
    private TextFieldWithBrowseButton myRepoField;

    public void showDialog(final Project project, final Runnable callback) {
      final DialogBuilder builder = new DialogBuilder(project);
      builder.setCenterPanel(createPanel(project, new Consumer<Boolean>() {
        @Override
        public void consume(Boolean aBoolean) {
          builder.setOkActionEnabled(aBoolean);
        }
      }));
      builder.addOkAction();
      builder.addCancelAction();
      builder.setDimensionServiceKey(getClass().getName());
      builder.setTitle("Open Fossil Repository");
      builder.setOkOperation(new Runnable() {
        @Override
        public void run() {
          builder.getWindow().setVisible(false);
          callback.run();
        }
      });
      builder.setPreferredFocusComponent(myRepoField);
      builder.show();
    }

    public String getLocalPath() {
      return myLocalPath.getText();
    }

    public String getRepo() {
      return myRepoField.getText();
    }

    private JComponent createPanel(final Project project, final Consumer<Boolean> enableConsumer) {
      final JPanel main = new JPanel(new GridBagLayout());
      main.setMinimumSize(new Dimension(150, 50));
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(2,2,2,2);
      gbc.anchor = GridBagConstraints.NORTHWEST;

      main.add(new JLabel("Repository file: "), gbc);
      myRepoField = new TextFieldWithBrowseButton();
      myRepoField.addBrowseFolderListener("Select Repository", null, project, new FileChooserDescriptor(true, false, false, false, false, false));

      gbc.gridx ++;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      main.add(myRepoField, gbc);
      gbc.gridx = 0;
      gbc.gridy ++;
      gbc.weightx = 0;
      gbc.fill = GridBagConstraints.NONE;
      main.add(new JLabel("Local Folder: "), gbc);
      myLocalPath = new TextFieldWithBrowseButton();
      myLocalPath.addBrowseFolderListener("Select Folder", null, project, new FileChooserDescriptor(false, true, false, false, false, false));
      /*myLocalPath.addBrowseFolderListener("Select Local File", "Select local file for clone", project,
              new FileSaverDescriptor("Fossil Clone", "Select local file", "checkout", ""));*/
      gbc.gridx ++;
      gbc.weightx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      main.add(myLocalPath, gbc);

      /*final ActionListener listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          enableConsumer.consume(!myUrlField.getText().isEmpty() && !myLocalPath.getText().isEmpty());
        }
      };
      myUrlField.addActionListener(listener);
      myLocalPath.addActionListener(listener);*/
      return main;
    }
  }
}
