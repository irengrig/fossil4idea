package org.github.irengrig.fossil4idea.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.ui.VcsBalloonProblemNotifier;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Irina.Chernushina on 5/29/2014.
 */
public class UiManager {
  private final Project myProject;
  private Runner command;
  private final Object myLock;

  public UiManager(Project myProject) {
    this.myProject = myProject;
    myLock = new Object();
  }

  public void run() {
    synchronized (myLock) {
      if (command != null) return;
      ApplicationManager.getApplication().assertIsDispatchThread();
      command = new Runner(myProject);
      ApplicationManager.getApplication().executeOnPooledThread(command);
    }
  }

  public void stop() {
    synchronized (myLock) {
      if (command == null) return;
      ApplicationManager.getApplication().assertIsDispatchThread();
      command.stop();
      command = null;
    }
  }

  public boolean isRun() {
    synchronized (myLock) {
      return command != null;
    }
  }

  private static class Runner implements Runnable {
    private final Project myProject;
    private boolean myDispose;
    private FossilSimpleCommand myCommand;
    private final Object myLock;

    public Runner(final Project project) {
      myLock = new Object();
      myProject = project;
    }

    @Override
    public void run() {
      synchronized (myLock) {
        myCommand = new FossilSimpleCommand(myProject, new File(myProject.getBaseDir().getPath()), FCommandName.ui);
      }
      try {
        myCommand.run();
      } catch (VcsException e) {
        synchronized (myLock) {
          if (! myDispose) {
            VcsBalloonProblemNotifier.showOverVersionControlView(myProject, "Could not run Fossil web UI: " + e.getMessage(), MessageType.ERROR);
          }
        }
      }
    }

    public void stop() {
      final FossilSimpleCommand simpleCommand;
      synchronized (myLock) {
        myDispose = true;
        simpleCommand = myCommand;
      }
      simpleCommand.destroyProcess();
    }
  }
}
