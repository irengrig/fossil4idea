package org.jetbrains.fossil.local;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/23/13
 * Time: 10:07 PM
 */
public class RenamedChange extends Change {
  public RenamedChange(@Nullable final ContentRevision afterRevision) {
    super(afterRevision, afterRevision);
  }

  @Override
  public Type getType() {
    return Type.MOVED;
  }

  @Override
  public FileStatus getFileStatus() {
    return FileStatus.MODIFIED;
  }

  @Override
  public boolean isRenamed() {
    return false;
  }

  @Override
  public boolean isMoved() {
    return true;
  }

  @Override
  public String getMoveRelativePath(final Project project) {
    return "[unknown place]";
  }
}
