package org.github.irengrig.test;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsShowConfirmationOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.Assert;
import org.github.irengrig.fossil4idea.log.ArtifactInfo;
import org.github.irengrig.fossil4idea.log.CatWorker;
import org.github.irengrig.fossil4idea.log.CommitWorker;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 4:45 PM
 */
public class FossilCatTest extends BaseFossilTest {
  @Test
  public void testSimpleCat() throws Exception {
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

    final String newContent = "a new text";
    editFileInCommand(myProject, file, newContent);

    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final Change changeEdit = myChangeListManager.getChange(file);
    Assert.assertNotNull(changeEdit);
    Assert.assertTrue(FileStatus.MODIFIED.equals(changeEdit.getFileStatus()));

    final List<VcsException> commitEdit = myVcs.getCheckinEnvironment().commit(Collections.singletonList(changeEdit), "***");
    Assert.assertTrue(commitEdit == null || commitEdit.isEmpty());
    assertNoLocalChanges();

    final String cat = new CatWorker(myProject).cat(new File(file.getPath()), null);
    Assert.assertEquals(newContent, cat);
  }

  @Test
  public void testSimpleGetBaseRevision() throws Exception {
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

    final CommitWorker commitWorker = new CommitWorker(myProject);
    final File ioFile = new File(file.getPath());
    final String baseRevision = commitWorker.getBaseRevision(ioFile);
    Assert.assertNotNull(baseRevision);
    System.out.println("base revision: " + baseRevision);
    final ArtifactInfo artifactInfo = commitWorker.getArtifactInfo(baseRevision, ioFile);
    Assert.assertNotNull(artifactInfo);
    System.out.println("date: " + artifactInfo.getDate());
  }
}
