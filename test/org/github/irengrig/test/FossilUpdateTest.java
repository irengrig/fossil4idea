package org.github.irengrig.test;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.update.SequentialUpdatesContext;
import com.intellij.openapi.vcs.update.UpdateSession;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.checkout.CheckoutUtil;
import org.github.irengrig.fossil4idea.init.CreateUtil;
import org.jetbrains.io.LocalFileFinder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FossilUpdateTest extends BaseTwoRootedFossilTest {
  private File myAnother;
  private VirtualFile myAnotherVf;
  private File myAnotherRepo;

  @Override
  @Before
  public void setUp() throws Exception {
    myTwoProjects = true;
    super.setUp();
  }

  private void cloneAnotherRepo() throws VcsException {
    myAnotherRepo = new File(myContentTwo, "anotherRepo");
    new CheckoutUtil(myProject1).cloneRepo("file://" + myRepoFile, myAnotherRepo.getPath());
    myAnother = new File(myContentTwo, "another");
    CreateUtil.openRepoTo(myProject, myAnother, myAnotherRepo);
    myAnotherVf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(myAnother);
  }

  @Test
  public void testSimple() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile fileD = createFileInCommand("was.txt", "111");
    final VirtualFile fileE = createFileInCommand("edit.txt", "111");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Collection<Change> allChanges = myChangeListManager.getAllChanges();

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(new ArrayList<Change>(allChanges), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();
    cloneAnotherRepo();

    final VirtualFile file = createFileInCommand("a with space.txt", "111");
    final VirtualFile file1 = createFileInCommand("1.txt", "111");
    editFileInCommand(myProject, fileE, "222");
    deleteFileInCommand(myProject, fileD);

    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final Collection<Change> allChanges1 = myChangeListManager.getAllChanges();

    final List<VcsException> commit1 = myVcs.getCheckinEnvironment().commit(new ArrayList<Change>(allChanges1), "2***");
    Assert.assertTrue(commit1 == null || commit1.isEmpty());
    assertNoLocalChanges();

    final UpdateSession updateSession = FossilVcs.getInstance(myProject1).getUpdateEnvironment().updateDirectories(new FilePath[]{new FilePathImpl(myAnotherVf)}, UpdatedFiles.create(),
            new EmptyProgressIndicator(), new Ref<SequentialUpdatesContext>());
  }
}
