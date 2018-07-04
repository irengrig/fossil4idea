package org.github.irengrig.fossil4idea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 3/2/13
 * Time: 8:37 PM
 */
public class RootUtil {
  private final static String ourCheckoutFileName = "_FOSSIL_";
  private final static String newCheckoutFileName = ".fslckout";

  public static List<VirtualFile> getFossilRoots(final VirtualFile[] roots) {
    if (roots == null || roots.length == 0) return Collections.emptyList();
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (VirtualFile rootsUnderVc : roots) {
      VfsUtil.processFileRecursivelyWithoutIgnored(rootsUnderVc,
          new Processor<VirtualFile>() {
            @Override
            public boolean process(final VirtualFile virtualFile) {
              if (ourCheckoutFileName.equals(virtualFile.getName()) || newCheckoutFileName.equals(virtualFile.getName())) {
                result.add(virtualFile.getParent());
              }
              return true;
            }
          });
    }
    return result;
  }

  public static File getWcRoot(final File file) {
    File current = file;
    final LocalFileSystem lfs = LocalFileSystem.getInstance();
    VirtualFile virtualFile = lfs.refreshAndFindFileByIoFile(current);
    while (current != null) {
      if (virtualFile != null) {
        if (virtualFile.findChild(ourCheckoutFileName) != null || virtualFile.findChild(newCheckoutFileName) != null) return new File(virtualFile.getPath());
        virtualFile = virtualFile.getParent();
      } else {
        current = current.getParentFile();
      }
    }
    return null;
  }

  @Nullable
  public static String getRemoteUrl(final Project project, File root) throws VcsException {
    final File working = root.isDirectory() ? root : root.getParentFile();
    final FossilSimpleCommand command = new FossilSimpleCommand(project, working, FCommandName.remote_url);
    final String text = command.run().trim();
    if ("off".equals(text)) return null;
    return text;
  }
}
