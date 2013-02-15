package org.jetbrains.fossil;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.update.SequentialUpdatesContext;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vcs.update.UpdateSession;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/15/13
 * Time: 11:55 PM
 */
public class FossilUpdateEnvironment implements UpdateEnvironment {
  public FossilUpdateEnvironment(final FossilVcs fossilVcs) {
  }

  @Override
  public void fillGroups(final UpdatedFiles updatedFiles) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public UpdateSession updateDirectories(@NotNull final FilePath[] contentRoots, final UpdatedFiles updatedFiles, final ProgressIndicator progressIndicator, @NotNull final Ref<SequentialUpdatesContext> context) throws ProcessCanceledException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Nullable
  @Override
  public Configurable createConfigurable(final Collection<FilePath> files) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean validateOptions(final Collection<FilePath> roots) {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
