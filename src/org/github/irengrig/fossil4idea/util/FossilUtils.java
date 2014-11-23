package org.github.irengrig.fossil4idea.util;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by irengrig on 23.11.2014.
 */
public class FossilUtils {
    public static final Convertor<FilePath, File> FILE_PATH_FILE_CONVERTOR = new Convertor<FilePath, File>() {
      @Override
      public File convert(final FilePath filePath) {
        return filePath.getIOFile();
      }
    };

  public static <T> List<T> ensureList(@NotNull final Collection<T> coll) {
    return coll instanceof List ? (List<T>) coll : new ArrayList<T>(coll);
  }
}
