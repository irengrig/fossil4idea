package org.github.irengrig.fossil4idea;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/12/13
 * Time: 10:38 AM
 */
public class FossilConfigurable implements Configurable {
  private final Project myProject;
  private JPanel myPanel;
  private TextFieldWithBrowseButton myCommandLine;

  public FossilConfigurable(final Project project) {
    myProject = project;
    createUI();
  }

  private void createUI() {
    myPanel = new JPanel(new BorderLayout());

    final JLabel label = new JLabel("Fossil command line client: ");
    final JPanel wrapper = new JPanel();
    final BoxLayout boxLayout = new BoxLayout(wrapper, BoxLayout.X_AXIS);
    wrapper.setLayout(boxLayout);
    wrapper.add(label);
    myCommandLine = new TextFieldWithBrowseButton();
    myCommandLine.addBrowseFolderListener("Point to Fossil command line", null, myProject,
        new FileChooserDescriptor(true, false, false, false, false, false));
    wrapper.add(myCommandLine);
    myPanel.add(wrapper, BorderLayout.NORTH);
  }

  @Nls
  public String getDisplayName() {
    return FossilVcs.DISPLAY_NAME;
  }

  @Nullable
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  public JComponent createComponent() {
    return myPanel;
  }

  public boolean isModified() {
    return ! Comparing.equal(FossilConfiguration.getInstance(myProject).FOSSIL_PATH, myCommandLine.getText().trim());
  }

  public void apply() throws ConfigurationException {
    FossilConfiguration.getInstance(myProject).FOSSIL_PATH = myCommandLine.getText().trim();
  }

  public void reset() {
    myCommandLine.setText(FossilConfiguration.getInstance(myProject).FOSSIL_PATH);
  }

  public void disposeUIResources() {
  }
}
