package org.github.irengrig.fossil4idea.checkout;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.CheckoutProvider;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.util.Consumer;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.checkin.CheckinUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FossilCheckoutProvider implements CheckoutProvider {
  @Override
  public void doCheckout(@NotNull final Project project, @Nullable Listener listener) {
    final UIWorker uiWorker = new UIWorker();
    uiWorker.showDialog(project, new Runnable() {
      @Override
      public void run() {
        try {
          new CheckinUtil(project).cloneRepo(uiWorker.getUrl(), uiWorker.getLocalPath());
          VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil clone successful", MessageType.INFO);
        } catch (VcsException e) {
          VcsBalloonProblemNotifier.showOverVersionControlView(project, "Fossil clone failed: " + e.getMessage(), MessageType.ERROR);
        }
      }
    });
  }

  @Override
  public String getVcsName() {
    return FossilVcs.DISPLAY_NAME;
  }

  private static class UIWorker {
    private TextFieldWithBrowseButton myLocalPath;
    private JTextField myUrlField;
    private TextField myFileName;

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

    public String getFileName() {
      return myFileName.getText();
    }

    private JComponent createPanel(final Project project, final Consumer<Boolean> enableConsumer) {
      final JPanel main = new JPanel(new GridBagLayout());
      final GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(2,2,2,2);
      gbc.anchor = GridBagConstraints.NORTHWEST;

      main.add(new JLabel("Remote URL: "), gbc);
      myUrlField = new JTextField(50);
      gbc.gridx ++;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      main.add(myUrlField, gbc);
      gbc.gridx = 0;
      gbc.gridy ++;
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
