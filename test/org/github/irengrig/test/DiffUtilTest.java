package org.github.irengrig.test;

import org.github.irengrig.fossil4idea.local.DiffUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Irina.Chernushina on 5/31/2014.
 */
public class DiffUtilTest {
  @Test
  public void testSimpleRename() throws Exception {
    final String original = "package com.something;\n" +
            "\n" +
            "/**\n" +
            " * Created by Irina.Chernushina on 5/29/2014.\n" +
            " */\n" +
            "public class Test3 {//\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"***\");\n" +
            "    }\n" +
            "}\n";
    final String changed = "package com.something;\n" +
            "\n" +
            "/**\n" +
            " * Created by Irina.Chernushina on 5/29/2014.\n" +
            " */\n" +
            "public class Test4 {//\n" +
            "\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"***\");\n" +
            "    }\n" +
            "}\n";
    final String patch = "Index: src/com/something/Test4.java\n" +
            "==================================================================\n" +
            "--- src/com/something/Test4.java\n" +
            "+++ src/com/something/Test4.java\n" +
            "@@ -1,11 +1,11 @@\n" +
            " package com.something;\n" +
            "\n" +
            " /**\n" +
            "  * Created by Irina.Chernushina on 5/29/2014.\n" +
            "  */\n" +
            "-public class Test3 {//\n" +
            "+public class Test4 {//\n" +
            "\n" +
            "     public static void main(String[] args) {\n" +
            "         System.out.println(\"***\");\n" +
            "     }\n" +
            " }";
    final String test = new DiffUtil().execute(changed, patch, "test");
    Assert.assertEquals(original, test);
  }
}
