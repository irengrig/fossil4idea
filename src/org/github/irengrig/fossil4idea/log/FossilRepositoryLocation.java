package org.github.irengrig.fossil4idea.log;

import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.VcsException;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 8:14 PM
 */
public class FossilRepositoryLocation implements RepositoryLocation {
  private final File myFile;

  public FossilRepositoryLocation(final File file) {
    myFile = file;
  }

  @Override
  public String toPresentableString() {
    return myFile.getPath();
  }

  @Override
  public String getKey() {
    return myFile.getPath();
  }

  @Override
  public void onBeforeBatch() throws VcsException {
  }

  @Override
  public void onAfterBatch() {
  }
}
