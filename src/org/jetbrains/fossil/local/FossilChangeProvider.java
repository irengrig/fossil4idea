package org.jetbrains.fossil.local;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.dbCommitted.ChangeTypeEnum;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.fossil.FossilVcs;
import org.jetbrains.fossil.repository.FossilContentRevision;
import org.jetbrains.fossil.repository.FossilRevisionNumber;
import org.jetbrains.fossil.util.FilterDescendantIoFiles;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/12/13
 * Time: 12:10 PM
 */
public class FossilChangeProvider implements ChangeProvider {
  private final Project myProject;

  public FossilChangeProvider(final Project project) {
    myProject = project;
  }

  public void getChanges(VcsDirtyScope vcsDirtyScope, final ChangelistBuilder changelistBuilder,
                         ProgressIndicator progressIndicator, ChangeListManagerGate changeListManagerGate) throws VcsException {
    final Set<FilePath> dirs = vcsDirtyScope.getRecursivelyDirtyDirectories();
    final Set<FilePath> dirtyFiles = vcsDirtyScope.getDirtyFiles();
    for (FilePath dirtyFile : dirtyFiles) {
      if (dirtyFile.isDirectory()) {
        dirs.add(dirtyFile);
      } else {
        dirs.add(dirtyFile.getParentPath());
      }
    }

    final List<File> files = ObjectsConvertor.fp2jiof(dirs);
    FilterDescendantIoFiles.getInstance().doFilter(files);

    final PairConsumer<File, ChangeTypeEnum> consumer = new PairConsumer<File, ChangeTypeEnum>() {
      @Override
      public void consume(final File file, final ChangeTypeEnum changeTypeEnum) {
        changelistBuilder.processChange(createChange(file, changeTypeEnum), FossilVcs.getVcsKey());
      }
    };
    for (File root : files) {
      LocalUtil.reportChanges(myProject, root, consumer);
      // todo and ignored
      final LocalFileSystem lfs = LocalFileSystem.getInstance();
      LocalUtil.reportUnversioned(myProject, root, new Consumer<File>() {
        @Override
        public void consume(final File file) {
          final VirtualFile vf = lfs.findFileByIoFile(file);
          if (vf != null) {
            changelistBuilder.processUnversionedFile(vf);
          }
        }
      });
    }
  }

  private Change createChange(final File file, final ChangeTypeEnum changeTypeEnum) {
    return new Change(createBefore(file, changeTypeEnum), createAfter(file, changeTypeEnum));
  }

  private ContentRevision createBefore(final File file, final ChangeTypeEnum changeTypeEnum) {
    if (ChangeTypeEnum.ADD.equals(changeTypeEnum) || ChangeTypeEnum.ADD_PLUS.equals(changeTypeEnum)) {
      return null;
    }
    return new FossilContentRevision(createFilePath(file), new FossilRevisionNumber("HEAD", null));
  }

  private ContentRevision createAfter(final File file, final ChangeTypeEnum changeTypeEnum) {
    if (ChangeTypeEnum.DELETE.equals(changeTypeEnum)) {
      return null;
    }
    final FilePath filePath = createFilePath(file);
    if (filePath.getFileType() != null && ! filePath.isDirectory() && filePath.getFileType().isBinary()) {
      return new CurrentBinaryContentRevision(filePath);
    }
    return new CurrentContentRevision(filePath);
  }

  // seems that folders are not versioned
  private FilePath createFilePath(final File file) {
    if (! file.exists()) {
      return VcsContextFactory.SERVICE.getInstance().createFilePathOnDeleted(file, false);
    }
    return VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
  }

  public boolean isModifiedDocumentTrackingRequired() {
    return false; // todo as in svn & git
  }

  public void doCleanup(List<VirtualFile> virtualFiles) {
  }
}
