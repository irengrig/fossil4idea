package org.github.irengrig.fossil4idea.local;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.PatchReader;
import com.intellij.openapi.diff.impl.patch.PatchSyntaxException;
import com.intellij.openapi.diff.impl.patch.TextFilePatch;
import com.intellij.openapi.diff.impl.patch.apply.GenericPatchApplier;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.text.LineReader;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by Irina.Chernushina on 5/31/2014.
 */
public class DiffUtil {
  private final static Logger LOG = Logger.getInstance(DiffUtil.class);
  public String execute(final String text, String patchContent, final String fileName) throws VcsException {
    patchContent = reversePatch(patchContent);
    final PatchReader patchReader = new PatchReader(patchContent);
    try {
      patchReader.parseAllPatches();
    } catch (PatchSyntaxException e) {
      throw new VcsException("Patch syntax exception in: " + fileName, e);
    }
    final List<TextFilePatch> patches = patchReader.getPatches();
    if (patches.size() != 1) throw new VcsException("Not one file patch in provided char sequence in: " + fileName);

    final TextFilePatch patch = patches.get(0);
    final GenericPatchApplier applier = new GenericPatchApplier(text, patch.getHunks());
    if (! applier.execute()) {
      LOG.info("Patch apply problems for: " + fileName);
      applier.trySolveSomehow();
    }
    return applier.getAfter();
  }

  public static String reversePatch(String patchContent) {
    final String[] strings = patchContent.split("\n");
    final StringBuilder sb = new StringBuilder();
    int cnt = 0;
    String s = strings[cnt];

    while (cnt < strings.length) {
      // context
      while (contextOrHeader(s) && cnt < strings.length) {
        sb.append(s);
        if (cnt < (strings.length - 1)) sb.append("\n");
        ++ cnt;
        if (cnt >= strings.length) break;
        s = strings[cnt];
      }

      if (cnt >= strings.length) break;

      final StringBuilder minus = new StringBuilder();
      final StringBuilder plus = new StringBuilder();
      while (cnt < strings.length && (s.startsWith("+") || s.startsWith("-"))) {
        if (s.startsWith("+")) {
          minus.append("-").append(s.substring(1));
          minus.append("\n");
        } else {
          plus.append("+").append(s.substring(1));
          plus.append("\n");
        }
        ++cnt;
        if (cnt >= strings.length) break;
        s = strings[cnt];
      }
      sb.append(minus.toString());
      sb.append(plus.toString());
    }
    final String val = sb.toString();
    // cut \n back if was added in the end
    if (val.endsWith("\n")) return val.substring(0, val.length() - 1);
    return val;
  }

  private static boolean contextOrHeader(String s) {
    if (s.startsWith("+++")) return true;
    if (s.startsWith("---")) return true;
    return !s.startsWith("+") && !s.startsWith("-");
  }
}
