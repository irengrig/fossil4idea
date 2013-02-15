package org.jetbrains.fossil.repository;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 11:02 PM
 */
public class FossilContentRevision implements ContentRevision {
  private final FilePath myFilePath;
  private final FossilRevisionNumber myNumber;

  public FossilContentRevision(final FilePath filePath, final FossilRevisionNumber number) {
    myFilePath = filePath;
    myNumber = number;
  }

  @Nullable
  @Override
  public String getContent() throws VcsException {
    // todo not implemented
    return null;
  }

  @NotNull
  @Override
  public FilePath getFile() {
    return myFilePath;
  }

  @NotNull
  @Override
  public FossilRevisionNumber getRevisionNumber() {
    return myNumber;
  }
}
