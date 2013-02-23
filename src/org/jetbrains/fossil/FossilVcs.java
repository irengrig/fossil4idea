package org.jetbrains.fossil;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.checkin.FossilCheckinEnvironment;
import org.jetbrains.fossil.local.FossilChangeProvider;
import org.jetbrains.fossil.local.FossilRollbackEnvironment;
import org.jetbrains.fossil.local.FossilVfsListener;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/12/13
 * Time: 10:35 AM
 */
public class FossilVcs extends AbstractVcs {
  public static String NAME = "fossil";
  public static String DISPLAY_NAME = "Fossil";
  private FossilChangeProvider myChangeProvider;
  private static final VcsKey ourKey = createKey(NAME);
  private FossilVfsListener myVfsListener;

  public FossilVcs(Project project) {
    super(project, NAME);
  }

  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Override
  public Configurable getConfigurable() {
    return new FossilConfigurable(myProject);
  }

  @Override
  protected void activate() {
    myVfsListener = new FossilVfsListener(myProject);
  }

  @Override
  protected void deactivate() {
    if (myVfsListener != null) {
      Disposer.dispose(myVfsListener);
      myVfsListener = null;
    }
  }

  @Nullable
  @Override
  public ChangeProvider getChangeProvider() {
    if (myChangeProvider == null) {
      myChangeProvider = new FossilChangeProvider(myProject);
    }
    return myChangeProvider;
  }

  @Nullable
  @Override
  protected CheckinEnvironment createCheckinEnvironment() {
    return new FossilCheckinEnvironment(this);
  }

  @Nullable
  @Override
  protected RollbackEnvironment createRollbackEnvironment() {
    return new FossilRollbackEnvironment(this);
  }

  @Nullable
  @Override
  protected UpdateEnvironment createUpdateEnvironment() {
    return new FossilUpdateEnvironment(this);
  }

  public void checkVersion() {
    //todo
  }

  public static FossilVcs getInstance(final Project project) {
    return (FossilVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(NAME);
  }

  public static VcsKey getVcsKey() {
    return ourKey;
  }
}
