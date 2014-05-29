package org.github.irengrig.fossil4idea.pull;

import com.google.common.base.Strings;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MultiLineLabelUI;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.UIUtil;
import org.github.irengrig.fossil4idea.FossilConfiguration;
import org.github.irengrig.fossil4idea.util.TextUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 3/2/13
 * Time: 8:35 PM
 */
public class FossilUpdateConfigurable implements Configurable {
  private final Project myProject;
  private final Collection<FilePath> myRoots;
  private final Map<File, String> myCheckoutURLs;
  private final String myWarning;
  private final Map<File, JBTextField> myFields;

  public FossilUpdateConfigurable(final Project project, final Collection<FilePath> roots, Map<File, String> checkoutURLs, String warning) {
    myProject = project;
    myRoots = roots;
    myCheckoutURLs = checkoutURLs;
    myWarning = warning;
    myFields = new HashMap<File, JBTextField>();
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "Fossil Update Settings";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    final JBPanel panel = new JBPanel(new GridBagLayout());
    if (myRoots.size() > 1) {
      buildForMultiple(panel);
    } else {
      buildForOne(panel, myRoots.iterator().next());
    }
    return panel;
  }

  private void buildForOne(JBPanel panel, final FilePath root) {
    final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 2;
    final JBLabel comp = new JBLabel("Please select remote URL:");
    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
    panel.add(comp, c);
    final JBTextField value = new JBTextField();
    final String preset = myCheckoutURLs.get(root.getIOFile());
    if (preset != null) {
      value.setText(preset);
    }
    myFields.put(root.getIOFile(), value);
    ++ c.gridy;
    panel.add(value, c);
    addWarning(panel, c);
  }

  private void buildForMultiple(JBPanel panel) {
    final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
    c.gridwidth = 2;
    c.fill = GridBagConstraints.HORIZONTAL;
    final JBLabel comp = new JBLabel("Please select remote URLs for roots:");
    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
    panel.add(comp, c);
    c.gridwidth = 1;

    for (FilePath root : myRoots) {
      c.weighty = 0;
      c.gridx = 0;
      ++ c.gridy;
      c.fill = GridBagConstraints.NONE;
      panel.add(new JBLabel(root.getName() + " (" + root.getParentPath() + ")"), c);
      ++ c.gridx;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weighty = 1;
      final JBTextField field = new JBTextField();
      panel.add(field, c);
      myFields.put(root.getIOFile(), field);
      final String preset = myCheckoutURLs.get(root.getIOFile());
      if (preset != null) {
        field.setText(preset);
      }
    }
    addWarning(panel, c);
  }

  private void addWarning(JBPanel panel, GridBagConstraints c) {
    if (myWarning != null && myWarning.length() > 0) {
      ++ c.gridy;
      c.gridx = 0;
      c.gridwidth = 2;
      c.fill = GridBagConstraints.HORIZONTAL;
      final JLabel label = new JLabel(new TextUtil().insertLineCuts("Warning: " + myWarning));
      label.setUI(new MultiLineLabelUI());
      label.setForeground(SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());
      panel.add(label, c);
    }
  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
    final Map<File, String> urls = new HashMap<File, String>();
    for (Map.Entry<File, JBTextField> entry : myFields.entrySet()) {
      final String text = entry.getValue().getText();
      if (text != null && text.trim().length() > 0) {
        urls.put(entry.getKey(), text.trim());
      }
    }
    if (! urls.isEmpty()) {
      FossilConfiguration.getInstance(myProject).setRemoteUrls(urls);
    }
  }

  @Override
  public void reset() {
  }

  @Override
  public void disposeUIResources() {
  }
}
