package org.github.irengrig.fossil4idea;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.*;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.pull.FossilUpdateConfigurable;
import org.github.irengrig.fossil4idea.util.RootUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/15/13
 * Time: 11:55 PM
 */
public class FossilUpdateEnvironment implements UpdateEnvironment {
  private final FossilVcs myFossilVcs;

  public FossilUpdateEnvironment(final FossilVcs fossilVcs) {
    myFossilVcs = fossilVcs;
  }

  @Override
  public void fillGroups(final UpdatedFiles updatedFiles) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public UpdateSession updateDirectories(@NotNull final FilePath[] contentRoots, final UpdatedFiles updatedFiles,
      final ProgressIndicator progressIndicator, @NotNull final Ref<SequentialUpdatesContext> context) throws ProcessCanceledException {
    final List<VcsException> exceptions = new ArrayList<VcsException>();
    final FossilConfiguration configuration = FossilConfiguration.getInstance(myFossilVcs.getProject());
    final Map<File, String> remoteUrls = configuration.getRemoteUrls();

    for (FilePath contentRoot : contentRoots) {
      progressIndicator.checkCanceled();
      final String remoteUrl = remoteUrls.get(contentRoot.getIOFile());

      try {
        final FossilSimpleCommand pull = new FossilSimpleCommand(myFossilVcs.getProject(), contentRoot.getIOFile(), FCommandName.pull);
        if (remoteUrl != null && remoteUrl.length() > 0) {
          pull.addParameters(remoteUrl);
        }
        final String pullResult = pull.run();
        /*Round-trips: 2   Artifacts sent: 0  received: 2
Pull finished with 611 bytes sent, 925 bytes received*/
        final FossilSimpleCommand update = new FossilSimpleCommand(myFossilVcs.getProject(), contentRoot.getIOFile(), FCommandName.update);
//        update.addParameters("--debug");
//        update.addParameters("--verbose");
        final String out = update.run();
        parseUpdateOut(out, updatedFiles, contentRoot.getIOFile());
      } catch (VcsException e) {
        exceptions.add(e);
      }
    }
    final MyUpdateSession session = new MyUpdateSession(exceptions, progressIndicator.isCanceled());
    return session;
  }

  private final static Map<String, String> ourGroupsMapping = new HashMap<String, String>();
  private final static String MERGE_CONFLICTS2 = "merge conflicts in";
  private final static String MERGE_CONFLICTS1 = "*****";
  static {
    ourGroupsMapping.put("ADD", FileGroup.CREATED_ID);
    ourGroupsMapping.put("UPDATE", FileGroup.MODIFIED_ID);
    ourGroupsMapping.put("REMOVE", FileGroup.REMOVED_FROM_REPOSITORY_ID);
    ourGroupsMapping.put("MERGE", FileGroup.MERGED_ID);
  }

  private String extractMergePath(final String path) {
    if (! path.startsWith(MERGE_CONFLICTS1)) return null;
    final int idx = path.indexOf(MERGE_CONFLICTS2);
    if (idx == -1) return null;
    return path.substring(idx + MERGE_CONFLICTS2.length()).trim();
  }

  private void parseUpdateOut(String out, UpdatedFiles updatedFiles, File ioFile) {
    final String[] split = out.split("\n");
    for (String s : split) {
      final String mergePath = extractMergePath(s);
      if (mergePath != null) {
        final File file = new File(ioFile, mergePath);
        updatedFiles.getGroupById(FileGroup.MERGED_ID).remove(file.getPath());
        updatedFiles.getGroupById(FileGroup.MERGED_WITH_CONFLICT_ID).add(file.getPath(), FossilVcs.getVcsKey(), null);
      }
      final int idx = s.indexOf(' ');
      if (idx > 0 && idx < (s.length() - 1)) {
        final String groupId = ourGroupsMapping.get(s.substring(0, idx));
        if (groupId != null) {
          final String relative = s.substring(idx).trim();
          final File file = new File(ioFile, relative);
          updatedFiles.getGroupById(groupId).add(file.getPath(), FossilVcs.getVcsKey(), null);
        }
      }
    }
  }

  @Nullable
  @Override
  public Configurable createConfigurable(final Collection<FilePath> files) {
    final Map<File, String> checkoutURLs = new HashMap<File, String>(FossilConfiguration.getInstance(myFossilVcs.getProject()).getRemoteUrls());

    final StringBuilder warnings = new StringBuilder();
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      @Override
      public void run() {
        for (FilePath root : files) {
          try {
            final String remoteUrl = RootUtil.getRemoteUrl(myFossilVcs.getProject(), root.getIOFile());
            if (remoteUrl != null) {
              checkoutURLs.put(root.getIOFile(), remoteUrl);
            }
          } catch (VcsException e) {
            warnings.append(e.getMessage()).append('\n');
          }
        }
      }
    }, "Getting remote URLs", true, myFossilVcs.getProject());
    return new FossilUpdateConfigurable(myFossilVcs.getProject(), files, checkoutURLs, warnings.toString());
  }

  @Override
  public boolean validateOptions(final Collection<FilePath> roots) {
    return true;
  }

  private static class MyUpdateSession extends UpdateSessionAdapter {
    public MyUpdateSession(List<VcsException> exceptions, boolean isCanceled) {
      super(exceptions, isCanceled);
    }
  }
}
