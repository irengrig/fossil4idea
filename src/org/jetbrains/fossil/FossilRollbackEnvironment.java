package org.jetbrains.fossil;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/15/13
 * Time: 11:55 PM
 */
public class FossilRollbackEnvironment implements RollbackEnvironment {
  public FossilRollbackEnvironment(final FossilVcs fossilVcs) {
  }

  @Override
  public String getRollbackOperationName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void rollbackChanges(final List<Change> changes, final List<VcsException> vcsExceptions, @NotNull final RollbackProgressListener listener) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void rollbackMissingFileDeletion(final List<FilePath> files, final List<VcsException> exceptions, final RollbackProgressListener listener) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void rollbackModifiedWithoutCheckout(final List<VirtualFile> files, final List<VcsException> exceptions, final RollbackProgressListener listener) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void rollbackIfUnchanged(final VirtualFile file) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
