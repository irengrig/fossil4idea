package org.github.irengrig.fossil4idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.github.irengrig.fossil4idea.checkin.FossilCheckinEnvironment;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by Irina.Chernushina on 6/1/2014.
 */
public class FossilCommitAndPushExecutor implements CommitExecutor {
  private final Project project;

  public FossilCommitAndPushExecutor(Project project) {
    this.project = project;
  }

  @Nls
  @Override
  public String getActionText() {
    return "Commit and &Push...";
  }

  @NotNull
  @Override
  public CommitSession createCommitSession() {
    ((FossilCheckinEnvironment) FossilVcs.getInstance(project).createCheckinEnvironment()).setPush();
    return CommitSession.VCS_COMMIT;
  }
}
