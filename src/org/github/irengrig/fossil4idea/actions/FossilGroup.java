package org.github.irengrig.fossil4idea.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.actions.StandardVcsGroup;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.jetbrains.annotations.Nullable;

public class FossilGroup extends StandardVcsGroup {
  @Override
  public AbstractVcs getVcs(Project project) {
    return FossilVcs.getInstance(project);
  }

  @Nullable
  @Override
  public String getVcsName(Project project) {
    return FossilVcs.NAME;
  }
}
