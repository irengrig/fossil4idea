package org.jetbrains.fossil.repository;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 11:04 PM
 */
public class FossilRevisionNumber implements VcsRevisionNumber {
  // todo special HEAD revision??
  @NotNull
  private final String myHash;
  private final Date myDate;

  public FossilRevisionNumber(final String hash, @Nullable final Date date) {
    myHash = hash;
    myDate = date;
  }

  @Override
  public String asString() {
    return myHash;
  }

  @Override
  public int compareTo(final VcsRevisionNumber o) {
    if (o instanceof FossilRevisionNumber) {
      if (myDate != null && ((FossilRevisionNumber) o).myDate != null) {
        return myDate.compareTo(((FossilRevisionNumber) o).myDate);
      }
    }
    return 0;
  }

  @NotNull
  public String getHash() {
    return myHash;
  }

  public Date getDate() {
    return myDate;
  }
}
