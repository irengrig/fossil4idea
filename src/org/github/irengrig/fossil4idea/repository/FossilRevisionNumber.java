package org.github.irengrig.fossil4idea.repository;

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
  public final static FossilRevisionNumber UNKNOWN = new FossilRevisionNumber("0", null);

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

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final FossilRevisionNumber that = (FossilRevisionNumber) o;

    if (!myHash.equals(that.myHash)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myHash.hashCode();
  }
}
