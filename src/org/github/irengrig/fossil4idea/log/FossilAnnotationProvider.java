package org.github.irengrig.fossil4idea.log;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import org.github.irengrig.fossil4idea.FossilVcs;
import org.github.irengrig.fossil4idea.commandLine.FCommandName;
import org.github.irengrig.fossil4idea.commandLine.FossilSimpleCommand;
import org.github.irengrig.fossil4idea.local.MoveWorker;
import org.github.irengrig.fossil4idea.FossilException;
import org.github.irengrig.fossil4idea.repository.FossilRevisionNumber;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 9:11 PM
 */
public class FossilAnnotationProvider implements AnnotationProvider {
  private final FossilVcs myFossilVcs;

  public FossilAnnotationProvider(final FossilVcs fossilVcs) {
    myFossilVcs = fossilVcs;
  }

  @Override
  public FileAnnotation annotate(final VirtualFile file) throws VcsException {
    return annotate(file, new CommitWorker(myFossilVcs.getProject()).getBaseFileRevision(new File(file.getPath())));
  }

  @Override
  public FileAnnotation annotate(final VirtualFile file, final VcsFileRevision revision) throws VcsException {
    final File ioFile = new File(file.getPath());
    final FossilFileAnnotation annotation = new FossilFileAnnotation(myFossilVcs.getProject(),
        new CatWorker(myFossilVcs.getProject()).cat(ioFile, revision.getRevisionNumber().asString()),
        (FossilRevisionNumber) revision.getRevisionNumber(), file);
    final FossilSimpleCommand command = new FossilSimpleCommand(myFossilVcs.getProject(), MoveWorker.findParent(ioFile), FCommandName.annotate);
    command.addParameters(ioFile.getPath());
    String result = command.run();
    result = result.replace("\r", "");
    final String[] lines = result.split("\n");
    final CommitWorker commitWorker = new CommitWorker(myFossilVcs.getProject());
    final Map<String, ArtifactInfo> revisionMap = new HashMap<String, ArtifactInfo>();
    int i = 0;
    for (String line : lines) {
      final int spaceIdx = line.indexOf(' ');
      if (spaceIdx == -1) {
        throw new FossilException("Can not parse annotation, line: " + line);
      }
      final String hash = line.substring(0, spaceIdx);
      ArtifactInfo artifactInfo = revisionMap.get(hash);
      if (artifactInfo == null) {
        artifactInfo = commitWorker.getArtifactInfo(hash, ioFile);
        if (artifactInfo == null) {
          // can not get file information, it was renamed
          artifactInfo = new ArtifactInfo();
          artifactInfo.setHash(hash);
          artifactInfo.setDate(new Date(0));
        } else {
          revisionMap.put(hash, artifactInfo);
        }
      }
      annotation.registerLine(i, artifactInfo);
      ++ i;
    }
    return annotation;
  }

  @Override
  public boolean isAnnotationValid(final VcsFileRevision rev) {
    return true;
  }
}
