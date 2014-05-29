package org.github.irengrig.fossil4idea.util;

/**
 * Created by Irina.Chernushina on 5/29/2014.
 */
public class TextUtil {
  private final static int optimal = 100;
  private final int myLimit;

  public TextUtil() {
    this(-1);
  }

  public TextUtil(int limit) {
    this.myLimit = limit < 0 ? optimal : limit;
  }

  public String insertLineCuts(final String s) {
    if (s == null || s.length() <= myLimit) return s;
    final int idx = s.substring(0, myLimit).lastIndexOf(' ');
    if (idx > 0) {
      return s.substring(0, idx) + "\n" + insertLineCuts(s.substring(idx + 1));
    }
    final int idx2 = s.indexOf(' ', myLimit);
    if (idx2 == -1) return s;
    return s.substring(0, idx2) + "\n" + insertLineCuts(s.substring(idx2 + 1));
  }
}
