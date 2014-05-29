package org.github.irengrig.test;

import org.github.irengrig.fossil4idea.util.TextUtil;
import org.junit.Test;

/**
 * Created by Irina.Chernushina on 5/29/2014.
 */
public class TextUtilTest {
  @Test
  public void testSimple() throws Exception {
    final String s = "The ui command is intended for accessing the web interface from a local desktop. " +
            "The ui command binds to the loopback IP address only (and thus makes the web interface visible only on the local machine)" +
            " and it automatically start your web browser pointing at the server. " +
            "someverylongfortest13213243254354365465765765 " +
            "For cross-machine collaboration, use the server command, which binds on all IP addresses and does not try to start a web browser.";
    // test is for it's not eternal
    final String cuts = new TextUtil(15).insertLineCuts(s);
    System.out.println(cuts);
  }
}
