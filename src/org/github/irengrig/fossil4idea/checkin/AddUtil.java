package org.github.irengrig.fossil4idea.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.CollectionSplitter;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.Convertor;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.FossilVcs;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/14/13
 * Time: 6:56 PM
 */
public class AddUtil {
  public static void scheduleForAddition(final Project project, final List<File> list) throws VcsException {
    final List<File> checkList = new ArrayList<File>(list);
    for (Iterator<File> iterator = checkList.iterator(); iterator.hasNext(); ) {
      File file = iterator.next();
      if (file.isDirectory()) {
        iterator.remove();
      }
    }

    final List<List<File>> split = new CollectionSplitter<File>(5).split(checkList);
    for (List<File> files : split) {
      addImpl(project, files);
    }
  }

  public static void deleteImpl(final Project project, final List<File> files) throws VcsException {
    final File parent = tryFindCommonParent(project, files);
    if (parent != null) {
      final FossilSimpleCommand command = new FossilSimpleCommand(project, parent, FCommandName.delete);
      command.addParameters(ObjectsConvertor.convert(files, new Convertor<File, String>() {
        @Override
        public String convert(final File o) {
          return FileUtil.getRelativePath(parent, o);
        }
      }));
      final String run = command.run();
    } else {
      for (File file : files) {
        final FossilSimpleCommand command = new FossilSimpleCommand(project, file, FCommandName.delete);
        command.addParameters(".");
        command.run();
      }
    }
  }

  private static void addImpl(final Project project, final List<File> files) throws VcsException {
    final File parent = tryFindCommonParent(project, files);
    if (parent != null) {
      final FossilSimpleCommand command = new FossilSimpleCommand(project, parent, FCommandName.add);
      command.addParameters("--dotfiles");
      command.addParameters("--force");
      command.addParameters(ObjectsConvertor.convert(files, new Convertor<File, String>() {
        @Override
        public String convert(final File o) {
          return FileUtil.getRelativePath(parent, o);
        }
      }));
      final String run = command.run();
    } else {
      for (File file : files) {
        if (file.getParentFile() == null) continue;
        final FossilSimpleCommand command = new FossilSimpleCommand(project, file.getParentFile(), FCommandName.add);
        command.addParameters("--dotfiles");
        command.addParameters("--force");
        command.addParameters(file.getName());
        command.run();
      }
    }
  }

  public static File tryFindCommonParent(final Project project, final List<File> files) {
    if (files.isEmpty()) return null;
    //if (files.size() == 1) return files.get(0).getParentFile();

    final List<File> other = files.subList(1, files.size());
    final VirtualFile[] rootsUnderVcs = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(FossilVcs.getInstance(project));
    if (rootsUnderVcs == null || rootsUnderVcs.length == 0) return null;
    File current = files.get(0).getParentFile();
    while (current != null) {
      boolean ok = true;
      for (File file : other) {
        if (! FileUtil.isAncestor(current, file, false)) {
          ok = false;
          break;
        }
      }
      if (ok) break;
      current = current.getParentFile();
    }
    if (current != null) {
      for (VirtualFile rootsUnderVc : rootsUnderVcs) {
        if (FileUtil.isAncestor(new File(rootsUnderVc.getPath()), current, false)) return current;
      }
    }
    return null;
  }
}
