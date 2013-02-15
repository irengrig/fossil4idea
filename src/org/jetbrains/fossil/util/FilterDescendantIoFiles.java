package org.jetbrains.fossil.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.AbstractFilterChildren;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 10:51 PM
 */
public class FilterDescendantIoFiles extends AbstractFilterChildren<File> {
  private static FilterDescendantIoFiles ourInstance = new FilterDescendantIoFiles();

  public static FilterDescendantIoFiles getInstance() {
    return ourInstance;
  }

  @Override
  protected void sortAscending(final List<File> list) {
    Collections.sort(list);
  }

  @Override
  protected boolean isAncestor(final File parent, final File child) {
    return FileUtil.isAncestor(parent, child, false);
  }
}
