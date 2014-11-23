package org.github.irengrig.fossil4idea.local;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.util.FilterDescendantIoFiles;
import org.github.irengrig.fossil4idea.util.FossilUtils;

import java.io.File;
import java.io.IOException;
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

    final List<File> files = ObjectsConvertor.convert(dirs, FossilUtils.FILE_PATH_FILE_CONVERTOR);
    FilterDescendantIoFiles.getInstance().doFilter(files);

    for (File root : files) {
      LocalUtil.reportChanges(myProject, root, changelistBuilder);
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
    final FileDocumentManager instance = FileDocumentManagerImpl.getInstance();
    final Document[] documents = instance.getUnsavedDocuments();
    for (Document document : documents) {
      final VirtualFile file = instance.getFile(document);
      if (file != null) {
        final FileStatus status = changeListManagerGate.getStatus(file);
        if (status == null || FileStatus.NOT_CHANGED.equals(status)) {
          changelistBuilder.processChange(LocalUtil.createChange(myProject, new File(file.getPath()), FileStatus.MODIFIED), FossilVcs.getVcsKey());
        }
      }
    }
  }


  public boolean isModifiedDocumentTrackingRequired() {
    return true;
  }

  public void doCleanup(List<VirtualFile> virtualFiles) {
  }
}
