package org.jetbrains.test;

import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManagerImpl;
import com.intellij.openapi.vcs.changes.LocallyDeletedChange;
import com.intellij.openapi.vcs.changes.ui.RollbackProgressModifier;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 6:38 PM
 */
public class FossilRevertTest extends BaseFossilTest {
  @Test
  public void testRevertAddDeleteModification() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile fileA = createFileInCommand("a with space.txt", "111");
    final VirtualFile fileB = createFileInCommand("b with space.txt", "1112");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeA = assertChangeIsAdded(fileA);
    final Change changeB = assertChangeIsAdded(fileB);

    final ArrayList<Change> changes = new ArrayList<Change>();
    changes.add(changeA);
    changes.add(changeB);
    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(changes, "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    final VirtualFile fileC = createFileInCommand("c with space.txt", "1115674657348");
    final FilePath ioFileA = new FilePathImpl(new File(fileA.getPath()), false);
    deleteFileInCommand(myProject, fileA);
    editFileInCommand(myProject, fileB, "747474");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeC = assertChangeIsAdded(fileC);
    final Change changeModified = myChangeListManager.getChange(fileB);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeModified.getFileStatus()));
    final Change changeDeleted = myChangeListManager.getChange(ioFileA);
    Assert.assertTrue(FileStatus.DELETED.equals(changeDeleted.getFileStatus()));

    final List<Change> changesToRevert = new ArrayList<Change>();
    changesToRevert.add(changeC);
    changesToRevert.add(changeDeleted);
    changesToRevert.add(changeModified);
    final ArrayList<VcsException> vcsExceptions = new ArrayList<VcsException>();
    myVcs.getRollbackEnvironment().rollbackChanges(changesToRevert, vcsExceptions, createListener());
    Assert.assertTrue(vcsExceptions.isEmpty());
    assertNoLocalChanges();
  }

  private RollbackProgressModifier createListener() {
    return new RollbackProgressModifier(1.0, ProgressManagerImpl.getInstance().getProgressIndicator());
  }

  @Test
  public void testRevertLocallyDeleted() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile fileA = createFileInCommand("a with space.txt", "111");
    final VirtualFile fileB = createFileInCommand("b with space.txt", "1112");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeA = assertChangeIsAdded(fileA);
    final Change changeB = assertChangeIsAdded(fileB);

    final ArrayList<Change> changes = new ArrayList<Change>();
    changes.add(changeA);
    changes.add(changeB);
    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(changes, "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    setStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_NOTHING_SILENTLY);
    final FilePath ioFileA = new FilePathImpl(new File(fileA.getPath()), false);
    deleteFileInCommand(myProject, fileA);
    assertNoLocalChanges();
    final List<LocallyDeletedChange> deletedFiles = ((ChangeListManagerImpl) myChangeListManager).getDeletedFiles();
    Assert.assertTrue(deletedFiles != null && ! deletedFiles.isEmpty());
    Assert.assertEquals(ioFileA, deletedFiles.get(0).getPath());

    final ArrayList<VcsException> exceptions = new ArrayList<VcsException>();
    myVcs.getRollbackEnvironment().rollbackMissingFileDeletion(Collections.singletonList(ioFileA), exceptions,
        createListener());
    Assert.assertTrue(exceptions.isEmpty());
    assertNoLocalChanges();
    final List<LocallyDeletedChange> deletedFiles2 = ((ChangeListManagerImpl) myChangeListManager).getDeletedFiles();
    Assert.assertTrue(deletedFiles2 != null && deletedFiles2.isEmpty());
  }

  private Change assertChangeIsAdded(final VirtualFile fileA) {
    final Change change = myChangeListManager.getChange(fileA);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));
    return change;
  }
}
