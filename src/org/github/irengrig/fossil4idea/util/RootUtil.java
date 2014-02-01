package org.github.irengrig.fossil4idea.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 3/2/13
 * Time: 8:37 PM
 */
public class RootUtil {
  private final static String ourCheckoutFileName = "_FOSSIL_";

  public static List<VirtualFile> getFossilRoots(final VirtualFile[] roots) {
    if (roots == null || roots.length == 0) return Collections.emptyList();
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (VirtualFile rootsUnderVc : roots) {
      VfsUtil.processFileRecursivelyWithoutIgnored(rootsUnderVc,
          new Processor<VirtualFile>() {
            @Override
            public boolean process(final VirtualFile virtualFile) {
              if (ourCheckoutFileName.equals(virtualFile.getName())) {
                result.add(virtualFile.getParent());
              }
              return true;
            }
          });
    }
    return result;
  }
}
