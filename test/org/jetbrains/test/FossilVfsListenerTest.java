package org.jetbrains.test;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.junit.Test;

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
    startChangeProvider();
    final VirtualFile file = createFileInCommand("a.txt", "111");
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));
  }
}
