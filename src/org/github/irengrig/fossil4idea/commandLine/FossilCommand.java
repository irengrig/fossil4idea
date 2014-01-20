package org.github.irengrig.fossil4idea.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ProcessEventListener;
import com.intellij.util.EventDispatcher;
import com.intellij.util.Processor;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.github.irengrig.fossil4idea.FossilConfiguration;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/12/13
 * Time: 12:17 PM
 */
public abstract class FossilCommand {
  private static final Logger LOG = Logger.getInstance(FossilCommand.class.getName());

  protected final Project myProject;
  protected final GeneralCommandLine myCommandLine;
  private final File myWorkingDirectory;
  protected Process myProcess;
  private final Object myLock;
  private Integer myExitCode; // exit code or null if exit code is not yet available
  private boolean myCanceled;

  private final EventDispatcher<ProcessEventListener> myListeners = EventDispatcher.create(ProcessEventListener.class);

  private Processor<OutputStream> myInputProcessor; // The processor for stdin

  public FossilCommand(Project project, File workingDirectory, @NotNull FCommandName commandName) {
    myLock = new Object();
    myProject = project;
    myCommandLine = new GeneralCommandLine();
    myWorkingDirectory = workingDirectory;
    final FossilConfiguration configuration = FossilConfiguration.getInstance(project);
    final String path = StringUtil.isEmptyOrSpaces(configuration.FOSSIL_PATH) ?
        "fossil" : configuration.FOSSIL_PATH;
    myCommandLine.setExePath(path);
    myCommandLine.setWorkDirectory(workingDirectory);
    myCommandLine.addParameter(commandName.getName());
  }

  public void start() {
    synchronized (myLock) {
      checkNotStarted();

      try {
        myProcess = startProcess();
        if (myProcess != null) {
          startHandlingStreams();
        } else {
          FossilVcs.getInstance(myProject).checkVersion();
          myListeners.getMulticaster().startFailed(null);
        }
      } catch (Throwable t) {
        LOG.info(t);
        FossilVcs.getInstance(myProject).checkVersion();
        myListeners.getMulticaster().startFailed(t);
      }
    }
  }

  /**
   * Wait for process termination
   */
  public void waitFor() {
    checkStarted();
    try {
      if (myInputProcessor != null && myProcess != null) {
        myInputProcessor.process(myProcess.getOutputStream());
      }
    }
    finally {
      waitForProcess();
    }
  }

  public boolean isCanceled() {
    synchronized (myLock) {
      return myCanceled;
    }
  }

  public void cancel() {
    synchronized (myLock) {
      myCanceled = true;
      checkStarted();
      destroyProcess();
    }
  }

  protected void setExitCode(final int code) {
    synchronized (myLock) {
      myExitCode = code;
    }
  }

  public void addListener(final ProcessEventListener listener) {
    synchronized (myLock) {
      myListeners.addListener(listener);
    }
  }

  protected ProcessEventListener listeners() {
    synchronized (myLock) {
      return myListeners.getMulticaster();
    }
  }

  public void addParameters(@NonNls @NotNull String... parameters) {
    synchronized (myLock) {
      checkNotStarted();
      myCommandLine.addParameters(parameters);
    }
  }

  public void addParameters(List<String> parameters) {
    synchronized (myLock) {
      checkNotStarted();
      myCommandLine.addParameters(parameters);
    }
  }

  public abstract void destroyProcess();
  protected abstract void waitForProcess();

  protected abstract Process startProcess() throws ExecutionException;

  /**
   * Start handling process output streams for the handler.
   */
  protected abstract void startHandlingStreams();

  /**
   * check that process is not started yet
   *
   * @throws IllegalStateException if process has been already started
   */
  private void checkNotStarted() {
    if (isStarted()) {
      throw new IllegalStateException("The process has been already started");
    }
  }

  /**
   * check that process is started
   *
   * @throws IllegalStateException if process has not been started
   */
  protected void checkStarted() {
    if (! isStarted()) {
      throw new IllegalStateException("The process is not started yet");
    }
  }

  /**
   * @return true if process is started
   */
  public boolean isStarted() {
    synchronized (myLock) {
      return myProcess != null;
    }
  }
}
