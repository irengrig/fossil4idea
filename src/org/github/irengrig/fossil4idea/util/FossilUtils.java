package org.github.irengrig.fossil4idea.util;

import com.intellij.openapi.vcs.FilePath;
import com.intellij.util.containers.Convertor;

import java.io.File;

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
}
