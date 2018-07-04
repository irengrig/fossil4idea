package org.github.irengrig.fossil4idea.commandLine;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 8:32 PM
 */
public abstract class FossilTextCommand extends FossilCommand {
  private boolean myIsDestroyed;
  private OSProcessHandler myHandler;

  public FossilTextCommand(Project project, File workingDirectory, @NotNull FCommandName commandName) {
    super(project, workingDirectory, commandName);
  }

  @Override
  protected void waitForProcess() {
    if (myHandler != null) {
      while (true) {
        if(myHandler.waitFor(200)) break;
        final ProgressManager pm = ProgressManager.getInstance();
        if (pm.hasProgressIndicator()) {
          if (pm.getProgressIndicator().isCanceled()) {
            destroyProcess();
          }
        }
      }
    }
  }

  @Override
  protected Process startProcess() throws ExecutionException {
    if (myIsDestroyed) return null;
    final Process process = myCommandLine.createProcess();
    myHandler = new OSProcessHandler(process, myCommandLine.getCommandLineString());
    return myHandler.getProcess();
  }

  @Override
  protected void startHandlingStreams() {
    if (myIsDestroyed || myProcess == null) return;

    myHandler.addProcessListener(new ProcessListener() {
      public void startNotified(final ProcessEvent event) {
        // do nothing
      }

      public void processTerminated(final ProcessEvent event) {
        final int exitCode = event.getExitCode();
        try {
          setExitCode(exitCode);
          //cleanupEnv();   todo
          FossilTextCommand.this.processTerminated(exitCode);
        } finally {
          listeners().processTerminated(exitCode);
        }
      }

      public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed) {
        // do nothing
      }

      public void onTextAvailable(final ProcessEvent event, final Key outputType) {
        FossilTextCommand.this.onTextAvailable(event.getText(), outputType);
      }
    });
    myHandler.startNotify();
  }

  protected abstract void processTerminated(int exitCode);
  protected abstract void onTextAvailable(final String text, final Key outputType);

  @Override
  public void destroyProcess() {
    myIsDestroyed = true;
    if (myHandler != null) {
      myHandler.destroyProcess();
    }
  }
}
