package org.github.irengrig.fossil4idea.checkout;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckoutProvider;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.actions.CloneAndOpenAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FossilCheckoutProvider implements CheckoutProvider {
  @Override
  public void doCheckout(@NotNull final Project project, @Nullable Listener listener) {
    CloneAndOpenAction.executeMe(project, listener);
  }

  @Override
  public String getVcsName() {
    return FossilVcs.DISPLAY_NAME;
  }
  }
