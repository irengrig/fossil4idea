package org.github.irengrig.fossil4idea.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.github.irengrig.fossil4idea.repository.FossilContentRevision;
import org.jetbrains.annotations.Nullable;
import org.github.irengrig.fossil4idea.repository.FossilRevisionNumber;

import java.io.IOException;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 8:13 PM
 */
public class FossilFileRevision implements VcsFileRevision {
  private final String myAuthor;
  private final String myComment;
  private final FossilContentRevision myContentRevision;

  public FossilFileRevision(final Project project, final FilePath filePath, final FossilRevisionNumber revisionNumber,
                            final String author, final String comment) {
    myAuthor = author;
    myComment = comment;
    myContentRevision = new FossilContentRevision(project, filePath, revisionNumber);
  }

  @Nullable
  @Override
  public String getBranchName() {
    return null;
  }

  @Nullable
  @Override
  public RepositoryLocation getChangedRepositoryPath() {
    return new FossilRepositoryLocation(myContentRevision.getFile().getIOFile());
  }

  @Override
  public byte[] loadContent() throws IOException, VcsException {
    return getContent();
  }

  @Nullable
  @Override
  public byte[] getContent() throws IOException, VcsException {
    final String content = myContentRevision.getContent();
    return content == null ? null : content.getBytes();
  }

  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return myContentRevision.getRevisionNumber();
  }

  @Override
  public Date getRevisionDate() {
    return myContentRevision.getRevisionNumber().getDate();
  }

  @Nullable
  @Override
  public String getAuthor() {
    return myAuthor;
  }

  @Nullable
  @Override
  public String getCommitMessage() {
    return myComment;
  }
}
