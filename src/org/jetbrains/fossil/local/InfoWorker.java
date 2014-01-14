package org.jetbrains.fossil.local;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 12:11 AM
 */
public class InfoWorker {
  private final Project myProject;
  private final File myObj;
  @Nullable
  private final String myOptional;

  public InfoWorker(final Project project, final File obj, @Nullable String optional) {
    myProject = project;
    myObj = obj;
    myOptional = optional;
  }

  public FossilInfo getInfo() throws VcsException {
    final File workingDirectory = MoveWorker.findParent(myObj);
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, workingDirectory, FCommandName.info);
    if (myOptional != null) {
      command.addParameters(myOptional);
    } else {
      //command.addParameters(myObj.getPath());
    }
    final String result = command.run();
    return parse(result);
  }

  /*project-name: <unnamed>
  repository:   c:/
  local-root:   c:/
  user-home:    C:/
  project-code: d641b91ef25f83ba71ac19a4a10
  checkout:     --0d7d2 2013-02-23 19:47:15 UTC
  tags:         trunk
  comment:      initial empty check-in (user: __)*/

  private FossilInfo parse(String result) throws FossilException {
    result = result.replace("\r", "\n");
    final String[] split = result.split("\n");
    final Map<String, String> map = new HashMap<String, String>(8, 1);
    for (String s : split) {
      final int i = s.indexOf(":");
      if (i == -1) {
        throw new FossilException("Can not parse 'info' output, line: " + s);
      }
      map.put(s.substring(0, i), s.substring(i + 1));
    }
    final FossilInfo info = new FossilInfo();
    fillLine(map, "project-name", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setProjectName(s);
      }
    });
    fillLine(map, "repository", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setRepository(s);
      }
    });
    fillLine(map, "local-root", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setLocalPath(s);
      }
    });
    /*fillLine(map, "user-home", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setUserHome(s);
      }
    });*/
    fillLine(map, "project-code", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setProjectId(s);
      }
    });
    fillLine(map, "checkout", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        // todo parse revision!!!
      }
    });
    fillLine(map, "tags", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        // todo parse tags
      }
    });
    fillLine(map, "comment", new Consumer<String>() {
      @Override
      public void consume(final String s) {
        info.setComment(s);
      }
    });
    return info;
  }

  private void fillLine(final Map<String, String> info, final String key, final Consumer<String> consumer) throws FossilException {
    final String value = info.get(key);
    if (value == null) {
      throw new FossilException("Can not find info line: " + key);
    }
    consumer.consume(new String(value.trim()));
  }
}
