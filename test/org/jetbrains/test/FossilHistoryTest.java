package org.jetbrains.test;

import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.history.FileHistorySessionPartner;
import com.intellij.openapi.vcs.history.VcsAbstractHistorySession;
import com.intellij.openapi.vcs.history.VcsAppendableHistoryPartnerAdapter;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.jetbrains.fossil.log.CatWorker;
import org.jetbrains.fossil.log.CommitWorker;
import org.jetbrains.fossil.log.HistoryWorker;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 8:56 PM
 */
public class FossilHistoryTest extends BaseFossilTest {
  @Test
  public void testFileHistory() throws Exception {
    setStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, VcsShowConfirmationOption.Value.DO_ACTION_SILENTLY);
    final String wasText = "111";
    final VirtualFile file = createFileInCommand("a with space.txt", wasText);
    sleep(100);
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change = myChangeListManager.getChange(file);
    Assert.assertNotNull(change);
    Assert.assertTrue(FileStatus.ADDED.equals(change.getFileStatus()));

    final List<VcsException> commit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change), "***");
    Assert.assertTrue(commit == null || commit.isEmpty());
    assertNoLocalChanges();

    final File ioFile = new File(file.getPath());
    final String wasRev = new CommitWorker(myProject).getBaseRevision(ioFile);
    Assert.assertNotNull(wasRev);

    editFileInCommand(myProject, file, "222");
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change change2 = myChangeListManager.getChange(file);
    Assert.assertNotNull(change2);
    Assert.assertTrue(FileStatus.MODIFIED.equals(change2.getFileStatus()));

    final List<VcsException> commit2 = myVcs.getCheckinEnvironment().commit(Collections.singletonList(change2), "***");
    Assert.assertTrue(commit2 == null || commit2.isEmpty());
    assertNoLocalChanges();

    final VcsAppendableHistoryPartnerAdapter partner = new VcsAppendableHistoryPartnerAdapter();
    new HistoryWorker(myProject).report(new FilePathImpl(file), partner);
    final VcsAbstractHistorySession session = partner.getSession();
    Assert.assertTrue(session.getRevisionList().size() == 2);

    final String baseRev = new CommitWorker(myProject).getBaseRevision(ioFile);
    Assert.assertNotNull(baseRev);
    Assert.assertEquals(baseRev, session.getCurrentRevisionNumber().asString());

    //cat
    final String wasContent = new CatWorker(myProject).cat(ioFile, wasRev);
    Assert.assertEquals(wasText, wasContent);
  }
}
