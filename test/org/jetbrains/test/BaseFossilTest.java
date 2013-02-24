package org.jetbrains.test;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.builders.EmptyModuleFixtureBuilder;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.util.ui.UIUtil;
import junit.framework.Assert;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.FossilVcs;
import org.jetbrains.fossil.init.CreateUtil;
import org.jetbrains.fossil.local.FossilInfo;
import org.jetbrains.fossil.local.InfoWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
  protected File myBase;
  protected VirtualFile myBaseVf;
  protected ChangeListManager myChangeListManager;
  protected VcsDirtyScopeManager myDirtyScopeManager;
  protected ProjectLevelVcsManager myVcsManager;
  private TempDirTestFixture myTempDirTestFixture;

  @Before
  public void setUp() throws Exception {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
        final String key = "idea.load.plugins.id";
        System.setProperty(key, "fossil4idea");
        final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
        myTempDirTestFixture = fixtureFactory.createTempDirTestFixture();
        myTempDirTestFixture.setUp();

        final String tempDirPath = myTempDirTestFixture.getTempDirPath();
        new File(tempDirPath).mkdirs();
        String name = getClass().getName() + "." + new TestName().getMethodName();

        final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder = IdeaTestFixtureFactory.getFixtureFactory().
            createFixtureBuilder(name);
        myProjectFixture = testFixtureBuilder.getFixture();
        final ModuleFixtureBuilder builder = testFixtureBuilder.
            addModule(EmptyModuleFixtureBuilder.class).addContentRoot(tempDirPath);
        myProjectFixture.setUp();
        myProject = myProjectFixture.getProject();
        //builder.addContentRoot(myProject.getBasePath());

        myLocalFileSystem = LocalFileSystem.getInstance();
        createRepositoryTreeInside(tempDirPath);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private void createRepositoryTreeInside(final String tempDirPath) throws VcsException, IOException {
    myBase = new File(tempDirPath);
    myBaseVf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(myBase);
    Assert.assertNotNull(myBaseVf);
    final File repo = FileUtil.createTempDirectory(myBase, "repo", "");
    final File repoFile = new File(repo, "test.fossil");
    final CreateUtil createUtil = new CreateUtil(myProject, repoFile.getPath());
    createUtil.createRepository();
    createUtil.openRepoTo(myBase, repoFile);
    final String projectId = createUtil.getProjectId();
    Assert.assertNotNull(projectId);

    final InfoWorker infoWorker = new InfoWorker(myProject, myBase, null);
    final FossilInfo info = infoWorker.getInfo();
    Assert.assertEquals(projectId, info.getProjectId());
  }

  protected void startChangeProvider() {
    ((StartupManagerImpl) StartupManager.getInstance(myProject)).runPostStartupActivities();
    myChangeListManager = ChangeListManager.getInstance(myProject);
    ((ProjectComponent) myChangeListManager).projectOpened();
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(myProject);
    ((ProjectComponent) myDirtyScopeManager).projectOpened();
    // mapping
    myVcsManager = ProjectLevelVcsManager.getInstance(myProject);
    myVcsManager.setDirectoryMappings(Collections.singletonList(new VcsDirectoryMapping(myBaseVf.getPath(), FossilVcs.NAME)));
  }

  @After
  public void tearDown() throws Exception {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          myProjectFixture.tearDown();
          myTempDirTestFixture.tearDown();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public void setStandardConfirmation(final VcsConfiguration.StandardConfirmation op,
                                      final VcsShowConfirmationOption.Value value) {
    ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final AbstractVcs vcs = vcsManager.findVcsByName(FossilVcs.NAME);
    VcsShowConfirmationOption option = vcsManager.getStandardConfirmation(op, vcs);
    option.setValue(value);
  }

  public VirtualFile createFileInCommand(final String name, @Nullable final String content) {
    return createFileInCommand(myBaseVf, name, content);
  }

  public VirtualFile createFileInCommand(final VirtualFile parent, final String name, @Nullable final String content) {
    final Ref<VirtualFile> result = new Ref<VirtualFile>();
    new WriteCommandAction.Simple(myProject) {
      @Override
      protected void run() throws Throwable {
        try {
          VirtualFile file = parent.createChildData(this, name);
          if (content != null) {
            file.setBinaryContent(CharsetToolkit.getUtf8Bytes(content));
          }
          result.set(file);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.execute();
    return result.get();
  }

  public VirtualFile createDirInCommand(final VirtualFile parent, final String name) {
    final Ref<VirtualFile> result = new Ref<VirtualFile>();
    new WriteCommandAction.Simple(myProject) {
      @Override
      protected void run() throws Throwable {
        try {
          VirtualFile dir = parent.findChild(name);
          if (dir == null) {
            dir = parent.createChildDirectory(this, name);
          }
          result.set(dir);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.execute();
    return result.get();
  }

  protected void sleep(final int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      //
    }
  }
}
