package org.github.irengrig.fossil4idea;

import com.intellij.openapi.vcs.VcsException;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 10:20 PM
 *
 * marker exception
 */
public class FossilException extends VcsException {
  public FossilException(final String message) {
    super(message);
  }

  public FossilException(final Throwable throwable, final boolean isWarning) {
    super(throwable, isWarning);
  }

  public FossilException(final Throwable throwable) {
    super(throwable);
  }

  public FossilException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public FossilException(final String message, final boolean isWarning) {
    super(message, isWarning);
  }

  public FossilException(final Collection<String> messages) {
    super(messages);
  }
}
