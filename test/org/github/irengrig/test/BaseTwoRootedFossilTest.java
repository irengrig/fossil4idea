package org.github.irengrig.test;

import com.intellij.ide.startup.impl.StartupManagerImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
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
import com.intellij.util.PlatformUtilsCore;
import com.intellij.util.ui.UIUtil;
import junit.framework.Assert;
import org.github.irengrig.fossil4idea.FossilConfiguration;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.init.CreateUtil;
import org.github.irengrig.fossil4idea.local.FossilInfo;
import org.github.irengrig.fossil4idea.local.InfoWorker;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertTrue;

public class BaseTwoRootedFossilTest {
  protected LocalFileSystem myLocalFileSystem;
  protected IdeaProjectTestFixture myProjectFixture;
  protected Project myProject;
  protected File myBase;
  protected VirtualFile myBaseVf;
  protected ChangeListManager myChangeListManager;
  protected VcsDirtyScopeManager myDirtyScopeManager;
  protected ProjectLevelVcsManager myVcsManager;
  private TempDirTestFixture myTempDirTestFixture;
  protected boolean myTwoProjects;
  protected FossilVcs myVcs;
  protected File myRepoFile;
  private TempDirTestFixture myTempDirTestFixture1;
  private IdeaProjectTestFixture myProjectFixture1;
  protected Project myProject1;
  protected File myContentOne;
  protected File myContentTwo;

  @Before
  public void setUp() throws Exception {
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        try {
          final String key = "idea.load.plugins.id";
          System.setProperty(PlatformUtilsCore.PLATFORM_PREFIX_KEY, PlatformUtilsCore.COMMUNITY_PREFIX);
          System.setProperty(key, "com.intellij,fossil4idea");
          final IdeaTestFixtureFactory fixtureFactory = IdeaTestFixtureFactory.getFixtureFactory();
          myTempDirTestFixture = fixtureFactory.createTempDirTestFixture();
          myTempDirTestFixture.setUp();
          // todo same myTempDirTestFixture, deeper project level
          final String tempDirPath = myTempDirTestFixture.getTempDirPath();
          myContentOne = new File(tempDirPath);
          myContentOne.mkdirs();
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

          if (myTwoProjects) {
            myTempDirTestFixture1 = fixtureFactory.createTempDirTestFixture();
            myTempDirTestFixture1.setUp();

            final String tempDirPath1 = myTempDirTestFixture1.getTempDirPath();
            myContentTwo = new File(tempDirPath1);
            myContentTwo.mkdirs();
            Assert.assertNotSame(myContentOne, myContentTwo);
            String name1 = getClass().getName() + "." + new TestName().getMethodName() + "1";

            final TestFixtureBuilder<IdeaProjectTestFixture> testFixtureBuilder1 = IdeaTestFixtureFactory.getFixtureFactory().
                    createFixtureBuilder(name1);
            myProjectFixture1 = testFixtureBuilder1.getFixture();
            final ModuleFixtureBuilder builder1 = testFixtureBuilder1.
                    addModule(EmptyModuleFixtureBuilder.class).addContentRoot(tempDirPath1);
            myProjectFixture1.setUp();
            myProject1 = myProjectFixture1.getProject();
          }

          setCorrectFossilPath();
          createRepositoryTreeInside(tempDirPath);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      private void setCorrectFossilPath() {
        if (new File(".\\util\\fossil.exe").exists()) {
          FossilConfiguration.getInstance(myProject).FOSSIL_PATH = new File(".\\util\\fossil.exe").getAbsolutePath();
          if (myProject1 != null) {
            FossilConfiguration.getInstance(myProject1).FOSSIL_PATH = new File(".\\util\\fossil.exe").getAbsolutePath();
          }
        }
      }
    });
    startChangeProvider();
    myVcs = FossilVcs.getInstance(myProject);
  }

  private VirtualFile createRepositoryTreeInside(final String tempDirPath) throws VcsException, IOException {
    myBase = FileUtil.createTempDirectory(new File(tempDirPath), "foss_co", "");
    myBaseVf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(myBase);
    Assert.assertNotNull(myBaseVf);
    final File repo = FileUtil.createTempDirectory(new File(tempDirPath), "foss_repo", "");
    myRepoFile = new File(repo, "test.fossil");
    final CreateUtil createUtil = new CreateUtil(myProject, myRepoFile.getPath());
    createUtil.createRepository();
    CreateUtil.openRepoTo(myProject, myBase, myRepoFile);
    final String projectId = createUtil.getProjectId();
    Assert.assertNotNull(projectId);

    final InfoWorker infoWorker = new InfoWorker(myProject, myBase, null);
    final FossilInfo info = infoWorker.getInfo();
    Assert.assertEquals(projectId, info.getProjectId());
    return myBaseVf;
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

          if (myProjectFixture1 != null) {
            myProjectFixture1.tearDown();
            myTempDirTestFixture1.tearDown();
          }
        } catch (Exception e) {
          //throw new RuntimeException(e);
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

  public static void deleteFileInCommand(final Project project, final VirtualFile file) {
    new WriteCommandAction.Simple(project) {
      @Override
      protected void run() throws Throwable {
        try {
          file.delete(this);
        }
        catch(IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    }.execute();
  }

  protected void assertNoLocalChanges() {
    myDirtyScopeManager.markEverythingDirty();
    myChangeListManager.ensureUpToDate(false);
    final List<LocalChangeList> changeListsCopy = myChangeListManager.getChangeListsCopy();
    int cnt = 0;
    for (LocalChangeList localChangeList : changeListsCopy) {
      cnt += localChangeList.getChanges().size();
    }
    Assert.assertEquals(0, cnt);
  }

  public static void editFileInCommand(final Project project, final VirtualFile file, final String newContent) {
    assertTrue(file.isValid());
    file.getTimeStamp();
    new WriteCommandAction.Simple(project) {
      @Override
      protected void run() throws Throwable {
        try {
          long newModTs = Math.max(System.currentTimeMillis(), file.getModificationStamp() + 1100);
          final long newTs = Math.max(System.currentTimeMillis(), file.getTimeStamp() + 1100);
          file.setBinaryContent(newContent.getBytes(), newModTs, newTs);
          final File file1 = new File(file.getPath());
          FileUtil.writeToFile(file1, newContent.getBytes());
          file.refresh(false, false);
          newModTs = Math.max(System.currentTimeMillis() + 1100, file.getModificationStamp() + 1100);
          assertTrue(file1 + " / " + newModTs, file1.setLastModified(newModTs));
        }
        catch(IOException ex) {
          throw new RuntimeException(ex);
        }
      }
    }.execute();
  }

  public static void renameFileInCommand(final Project project, final VirtualFile file, final String newName) {
    new WriteCommandAction.Simple(project) {
      @Override
      protected void run() throws Throwable {
        try {
          file.rename(this, newName);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.execute().throwException();
  }

  public static void moveFileInCommand(final Project project, final VirtualFile file, final VirtualFile newParent) {
    new WriteCommandAction.Simple(project) {
      @Override
      protected void run() throws Throwable {
        try {
          file.move(this, newParent);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }.execute();
  }
}
