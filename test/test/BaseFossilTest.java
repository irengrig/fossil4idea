package org.jetbrains.test;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.util.ui.UIUtil;
import junit.framework.Assert;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.init.CreateUtil;
import org.jetbrains.fossil.local.FossilInfo;
import org.jetbrains.fossil.local.InfoWorker;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 11:25 PM
 */
public class BaseFossilTest {
  protected LocalFileSystem myLocalFileSystem;
  protected IdeaProjectTestFixture myProjectFixture;
  protected Project myProject;

  @Before
  public void setUp() throws Exception {
    final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().createFixtureBuilder();
    myProjectFixture = testFixtureBuilder.getFixture();
    myProjectFixture.setUp();
    myProject = myProjectFixture.getProject();

    myLocalFileSystem = LocalFileSystem.getInstance();
    createRepositoryTreeInside();
  }

  private void createRepositoryTreeInside() throws VcsException, IOException {
    final File base = new File(myProject.getBasePath());
    final File repo = FileUtil.createTempDirectory(base, "repo", "");
    final File repoFile = new File(repo, "test.fossil");
    final CreateUtil createUtil = new CreateUtil(myProject, repoFile.getPath());
    createUtil.createRepository();
    createUtil.openRepoTo(base, repoFile);
    final String projectId = createUtil.getProjectId();
    Assert.assertNotNull(projectId);

    final InfoWorker infoWorker = new InfoWorker(myProject, base, null);
    final FossilInfo info = infoWorker.getInfo();
    Assert.assertEquals(projectId, info.getProjectId());
  }

  @After
  public void tearDown() throws Exception {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          myProjectFixture.tearDown();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }
}
