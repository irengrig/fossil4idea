package org.jetbrains.test;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 3:44 PM
 */
public class FossilCheckinTest extends BaseFossilTest {
  @Test
  public void testSimpleCheckin() throws Exception {
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
  }

  @Test
  public void testModificationCheckin() throws Exception {
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

    editFileInCommand(myProject, file, "981230213dh2bdbwcwed26y876er32178dewhdjw");

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeEdit = myChangeListManager.getChange(file);
    Assert.assertNotNull(changeEdit);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeEdit.getFileStatus()));

    final List<VcsException> commitEdit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(changeEdit), "***");
    Assert.assertTrue(commitEdit == null || commitEdit.isEmpty());
    assertNoLocalChanges();
  }
}
