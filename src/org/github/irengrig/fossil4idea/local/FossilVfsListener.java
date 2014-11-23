package org.github.irengrig.fossil4idea.local;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.Convertor;
import com.intellij.vcsUtil.VcsFileUtil;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.checkin.AddUtil;
import org.github.irengrig.fossil4idea.util.FossilUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 10:30 PM
 */
public class FossilVfsListener extends VcsVFSListener {
  public FossilVfsListener(final Project project) {
    super(project, FossilVcs.getInstance(project));
  }

  @Override
  protected String getAddTitle() {
    return "Add Files To Fossil";
  }

  @Override
  protected String getSingleFileAddTitle() {
    return "Add File to Fossil";
  }

  @Override
  protected String getSingleFileAddPromptTemplate() {
    return "Do you want to add following file to Fossil?\n{0}\n\n" +
        "If you say No, you can still add it later manually.";
  }

  @Override
  protected void performAdding(final Collection<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copyFromMap) {
    try {
      AddUtil.scheduleForAddition(myProject, ObjectsConvertor.convert(addedFiles, new Convertor<VirtualFile, File>() {
        @Override
        public File convert(final VirtualFile o) {
          return new File(o.getPath());
        }
      }));
      VcsFileUtil.markFilesDirty(myProject, ObjectsConvertor.vf2fp(FossilUtils.ensureList(addedFiles)));
    } catch (VcsException e) {
      myExceptions.add(e);
    }
  }

  @Override
  protected String getDeleteTitle() {
    return "Delete Files from Fossil";
  }

  @Override
  protected String getSingleFileDeleteTitle() {
    return "Delete File from Fossil";
  }

  @Override
  protected String getSingleFileDeletePromptTemplate() {
    return "Do you want to delete the following file from Fossil?\\n{0}\\n\\nIf you say No, you can still delete it later manually.";
  }

  @Override
  protected void performDeletion(final List<FilePath> filesToDelete) {
    try {
      AddUtil.deleteImpl(myProject, ObjectsConvertor.convert(filesToDelete, FossilUtils.FILE_PATH_FILE_CONVERTOR));
      VcsFileUtil.markFilesDirty(myProject, filesToDelete);
    } catch (VcsException e) {
      myExceptions.add(e);
    }
  }

  @Override
  protected void performMoveRename(final List<MovedFileInfo> movedFiles) {
    for (MovedFileInfo movedFile : movedFiles) {
      singleMoveRename(movedFile);
    }
  }

  private void singleMoveRename(final MovedFileInfo movedFile) {
    final File oldPath = new File(movedFile.myOldPath);
    final File newPath = new File(movedFile.myNewPath);
    final boolean isRename = FileUtil.filesEqual(oldPath.getParentFile(), newPath.getParentFile());
    final MoveWorker moveWorker = new MoveWorker(myProject);
    try {
      if (isRename) {
        moveWorker.doRename(oldPath, newPath);
      } else {
        moveWorker.doMove(oldPath, newPath.getParentFile());
        if (! FileUtil.namesEqual(oldPath.getName(), newPath.getName())) {
          // + rename
          moveWorker.doRename(new File(newPath.getParentFile(), oldPath.getName()), newPath);
        }
      }
    } catch (VcsException e) {
      myExceptions.add(e);
    }
  }

  @Override
  protected boolean isDirectoryVersioningSupported() {
    return false;
  }
}
