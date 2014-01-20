package org.github.irengrig.fossil4idea;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import org.github.irengrig.fossil4idea.checkin.FossilCheckinEnvironment;
import org.github.irengrig.fossil4idea.local.FossilChangeProvider;
import org.github.irengrig.fossil4idea.local.FossilVfsListener;
import org.github.irengrig.fossil4idea.log.FossilHistoryProvider;
import org.jetbrains.annotations.Nullable;
import org.github.irengrig.fossil4idea.local.FossilRollbackEnvironment;
import org.github.irengrig.fossil4idea.log.FossilAnnotationProvider;

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
  public VcsHistoryProvider getVcsHistoryProvider() {
    return new FossilHistoryProvider(this);
  }

  @Nullable
  @Override
  protected UpdateEnvironment createUpdateEnvironment() {
    return new FossilUpdateEnvironment(this);
  }

  public void checkVersion() {
    //todo
  }

  @Nullable
  @Override
  public DiffProvider getDiffProvider() {
    return new FossilDiffProvider(this);
  }

  public static FossilVcs getInstance(final Project project) {
    return (FossilVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(NAME);
  }

  public static VcsKey getVcsKey() {
    return ourKey;
  }

  @Nullable
  @Override
  public AnnotationProvider getAnnotationProvider() {
    return new FossilAnnotationProvider(this);
  }
}
