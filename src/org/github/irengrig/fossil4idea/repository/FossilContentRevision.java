package org.github.irengrig.fossil4idea.repository;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Throwable2Computable;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.impl.ContentRevisionCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.log.CatWorker;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 11:02 PM
 */
public class FossilContentRevision implements ContentRevision {
  private final Project myProject;
  private final FilePath myFilePath;
  private final FossilRevisionNumber myNumber;

  public FossilContentRevision(final Project project, final FilePath filePath, final FossilRevisionNumber number) {
    myProject = project;
    myFilePath = filePath;
    myNumber = number;
  }

  @Nullable
  @Override
  public String getContent() throws VcsException {
    try {
      return ContentRevisionCache.getOrLoadAsString(myProject, myFilePath, myNumber, FossilVcs.getVcsKey(),
          ContentRevisionCache.UniqueType.REPOSITORY_CONTENT, new Throwable2Computable<byte[], VcsException, IOException>() {
        @Override
        public byte[] compute() throws VcsException, IOException {
          return new CatWorker(myProject).cat(myFilePath.getIOFile(), myNumber.getHash()).getBytes();
        }
      });
    } catch (IOException e) {
      throw new FossilException(e);
    }
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

  public Project getProject() {
    return myProject;
  }
}
