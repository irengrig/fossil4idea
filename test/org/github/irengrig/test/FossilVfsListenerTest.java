package org.github.irengrig.test;

import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 11:21 PM
 */
public class FossilVfsListenerTest extends BaseFossilTest {
  @Test
  public void testAddFile() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile file = createFileInCommand("a with space.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));
  }

  @Test
  public void testDelete() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile file = createFileInCommand("a with space.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    final FilePath delFilePath = new FilePathImpl(new File(file.getPath()), false);
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    deleteFileInCommand(myProject, file);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeDel = myChangeListManager.getChange(delFilePath);
    Assert.assertNotNull(changeDel);
    Assert.assertTrue(FileStatus.DELETED.equals(changeDel.getFileStatus()));
  }

  @Test
  public void testRenameTest() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile file = createFileInCommand("a with space.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    final File parent = new File(file.getParent().getPath());
    final String newName = "newName.txt";
    renameFileInCommand(myProject, file, newName);
    editFileInCommand(myProject, file, "12323423534534");
    Assert.assertTrue(file != null && file.isValid() && newName.equals(file.getName()));

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeRenamed = myChangeListManager.getChange(file);
    Assert.assertNotNull(changeRenamed);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeRenamed.getFileStatus()));
//    Assert.assertTrue(changeRenamed.isMoved());
  }

  @Test
  public void testMoveTest() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile file = createFileInCommand("a with space.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    final VirtualFile dir = createDirInCommand(myBaseVf, "dir");
    final File parent = new File(file.getParent().getPath());
    moveFileInCommand(myProject, file, dir);
    editFileInCommand(myProject, file, "5239857239785239");
    Assert.assertTrue(file != null && file.isValid());

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeRenamed = myChangeListManager.getChange(file);
    Assert.assertNotNull(changeRenamed);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeRenamed.getFileStatus()));
//    Assert.assertTrue(changeRenamed.isMoved());
  }

  @Test
  public void testRenameDirTest() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final VirtualFile dir = createDirInCommand(myBaseVf, "dir");
    final VirtualFile file = createFileInCommand(dir, "a with space.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    renameFileInCommand(myProject, dir, "newName");
    editFileInCommand(myProject, file, "423423523");
    Assert.assertTrue(file != null && file.isValid());

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeRenamed = myChangeListManager.getChange(file);
//    Assert.assertNotNull(changeRenamed);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeRenamed.getFileStatus()));
//    Assert.assertTrue(changeRenamed.isMoved());
  }
}
