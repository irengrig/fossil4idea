package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.github.irengrig.fossil4idea.checkin.CheckinUtil;

/**
 * Created by Irina.Chernushina on 6/1/2014.
 */
public class PushAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
    if (project == null) return;

    new CheckinUtil(project).push();
  }
}
