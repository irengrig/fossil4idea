package org.jetbrains.fossil.checkin;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.FunctionUtil;
import com.intellij.util.NullableFunction;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.FossilVcs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/14/13
 * Time: 6:52 PM
 */
public class FossilCheckinEnvironment implements CheckinEnvironment {
  private final FossilVcs myFossilVcs;

  public FossilCheckinEnvironment(final FossilVcs fossilVcs) {
    myFossilVcs = fossilVcs;
  }

  @Nullable
  @Override
  public RefreshableOnComponent createAdditionalOptionsPanel(final CheckinProjectPanel panel, final PairConsumer<Object, Object> additionalDataConsumer) {
    return null;
  }

  @Nullable
  @Override
  public String getDefaultMessageFor(final FilePath[] filesToCheckin) {
    return null;
  }

  @Nullable
  @Override
  public String getHelpId() {
    return null;
  }

  @Override
  public String getCheckinOperationName() {
    return "Commit";
  }

  @Nullable
  @Override
  public List<VcsException> commit(final List<Change> changes, final String preparedComment) {
    return commit(changes, preparedComment, FunctionUtil.<Object, Object>nullConstant(), null);
  }

  @Nullable
  @Override
  public List<VcsException> commit(final List<Change> changes, final String preparedComment,
                                   @NotNull final NullableFunction<Object, Object> parametersHolder, final Set<String> feedback) {
    throw new UnsupportedOperationException();
  }

  @Nullable
  @Override
  public List<VcsException> scheduleMissingFileForDeletion(final List<FilePath> files) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Nullable
  @Override
  public List<VcsException> scheduleUnversionedFilesForAddition(final List<VirtualFile> files) {
    final List<VcsException> result = new ArrayList<VcsException>();
    try {
      AddUtil.scheduleForAddition(myFossilVcs.getProject(), ObjectsConvertor.convert(files,
          new Convertor<VirtualFile, File>() {
            @Override
            public File convert(final VirtualFile o) {
              return new File(o.getPath());
            }
          }));
    } catch (VcsException e) {
      result.add(e);
    }
    return result;
  }

  @Override
  public boolean keepChangeListAfterCommit(final ChangeList changeList) {
    return false;
  }

  @Override
  public boolean isRefreshAfterCommitNeeded() {
    return false;
  }
}
