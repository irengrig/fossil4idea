package org.jetbrains.fossil;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.log.CatWorker;
import org.jetbrains.fossil.log.CommitWorker;
import org.jetbrains.fossil.repository.FossilContentRevision;
import org.jetbrains.fossil.repository.FossilRevisionNumber;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 3/2/13
 * Time: 7:47 PM
 */
public class FossilDiffProvider implements DiffProvider {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.fossil.FossilDiffProvider");
  private final FossilVcs myFossilVcs;

  public FossilDiffProvider(final FossilVcs fossilVcs) {
    myFossilVcs = fossilVcs;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevision(final VirtualFile file) {
    try {
      return new CommitWorker(myFossilVcs.getProject()).getBaseRevisionNumber(new File(file.getPath()));
    } catch (VcsException e) {
      LOG.info(e);
      return null;
    }
  }

  @Nullable
  @Override
  public ItemLatestState getLastRevision(final VirtualFile virtualFile) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public ItemLatestState getLastRevision(final FilePath filePath) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public ContentRevision createFileContent(final VcsRevisionNumber revisionNumber, final VirtualFile selectedFile) {
    return new FossilContentRevision(myFossilVcs.getProject(), new FilePathImpl(selectedFile), (FossilRevisionNumber) revisionNumber);
  }

  @Nullable
  @Override
  public VcsRevisionNumber getLatestCommittedRevision(final VirtualFile vcsRoot) {
    throw new UnsupportedOperationException();
  }
}
