package org.github.irengrig.fossil4idea.log;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.github.irengrig.fossil4idea.FossilVcs;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 7:02 PM
 */
public class FossilHistoryProvider implements VcsHistoryProvider {
  private final FossilVcs myFossilVcs;

  public FossilHistoryProvider(final FossilVcs fossilVcs) {
    myFossilVcs = fossilVcs;
  }

  @Override
  public VcsDependentHistoryComponents getUICustomization(final VcsHistorySession session, final JComponent forShortcutRegistration) {
    return VcsDependentHistoryComponents.createOnlyColumns(new ColumnInfo[0]);
  }

  @Override
  public AnAction[] getAdditionalActions(final Runnable refresher) {
    return new AnAction[0];
  }

  @Override
  public boolean isDateOmittable() {
    return false;
  }

  @Nullable
  @Override
  public String getHelpId() {
    return null;
  }

  @Nullable
  @Override
  public VcsHistorySession createSessionFor(final FilePath filePath) throws VcsException {
    final VcsAppendableHistoryPartnerAdapter adapter = new VcsAppendableHistoryPartnerAdapter();
    reportAppendableHistory(filePath, adapter);
    adapter.check();

    return adapter.getSession();
  }

  @Override
  public void reportAppendableHistory(final FilePath path, final VcsAppendableHistorySessionPartner partner) throws VcsException {
    new HistoryWorker(myFossilVcs.getProject()).report(path, partner);
  }

  @Override
  public boolean supportsHistoryForDirectories() {
    return false;
  }

  @Nullable
  @Override
  public DiffFromHistoryHandler getHistoryDiffHandler() {
    return null;
  }

  @Override
  public boolean canShowHistoryFor(@NotNull final VirtualFile file) {
    return true;
  }
}
