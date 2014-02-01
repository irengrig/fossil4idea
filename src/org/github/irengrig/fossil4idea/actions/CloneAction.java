package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.Consumer;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.checkout.CheckoutUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CloneAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
    if (project == null) return;

    final UIWorker uiWorker = new UIWorker();
    uiWorker.showDialog(project, new Runnable() {
      @Override
      public void run() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Clone Fossil Repository", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
          @Override
          public void run(@NotNull ProgressIndicator progressIndicator) {
            try {
              new CheckoutUtil(project).cloneRepo(uiWorker.getUrl(), uiWorker.getLocalPath());
              VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil clone successful: " + uiWorker.getLocalPath(), MessageType.INFO);
            } catch (VcsException e) {
              VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil clone failed: " + e.getMessage(), MessageType.ERROR);
            }
          }
        });
      }
    });
  }

  private static class UIWorker {
    private TextFieldWithBrowseButton myLocalPath;
    private JTextField myUrlField;

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
      builder.setTitle("Clone Fossil Repository");
      builder.setOkOperation(new Runnable() {
        @Override
        public void run() {
          builder.getWindow().setVisible(false);
          callback.run();
        }
      });
      builder.setPreferredFocusComponent(myUrlField);
      builder.show();
    }

    public String getLocalPath() {
      return myLocalPath.getText();
    }

    public String getUrl() {
      return myUrlField.getText();
    }

    private JComponent createPanel(final Project project, final Consumer<Boolean> enableConsumer) {
      final JPanel main = new JPanel(new GridBagLayout());
      main.setMinimumSize(new Dimension(150, 50));
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(2,2,2,2);
      gbc.anchor = GridBagConstraints.NORTHWEST;

      main.add(new JLabel("Remote URL: "), gbc);
      myUrlField = new JTextField(50);
      gbc.gridx ++;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      main.add(myUrlField, gbc);
      gbc.gridx = 0;
      gbc.gridy ++;
      gbc.weightx = 0;
      gbc.fill = GridBagConstraints.NONE;
      main.add(new JLabel("Local Folder: "), gbc);
      myLocalPath = new TextFieldWithBrowseButton();
      myLocalPath.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                  new FileSaverDescriptor("Fossil Clone", "Select local file"), project);
          final String path = FileUtil.toSystemIndependentName(myLocalPath.getText().trim());
          final int idx = path.lastIndexOf("/");
          VirtualFile baseDir = idx == -1 ? project.getBaseDir() :
                  (LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path.substring(0, idx))));
          baseDir = baseDir == null ? project.getBaseDir() : baseDir;
          final String name = idx == -1 ? path : path.substring(idx + 1);
          final VirtualFileWrapper fileWrapper = dialog.save(baseDir, name);
          if (fileWrapper != null) {
            myLocalPath.setText(fileWrapper.getFile().getPath());
          }
        }
      });
      /*myLocalPath.addBrowseFolderListener("Select Local File", "Select local file for clone", project,
              new FileSaverDescriptor("Fossil Clone", "Select local file", "checkout", ""));*/
      gbc.weightx = 1;
      gbc.gridx ++;
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
