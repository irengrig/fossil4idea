package org.jetbrains.fossil.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.fossil.FossilException;
import org.jetbrains.fossil.commandLine.FCommandName;
import org.jetbrains.fossil.commandLine.FossilSimpleCommand;
import org.jetbrains.fossil.local.MoveWorker;
import org.jetbrains.fossil.repository.FossilRevisionNumber;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 4:57 PM
 */
public class CommitWorker {
  private final Project myProject;

  public CommitWorker(final Project project) {
    myProject = project;
  }

  public FossilRevisionNumber getBaseRevisionNumber(final File file) throws VcsException {
    final String baseRevision = getBaseRevision(file);
    final ArtifactInfo artifactInfo = getArtifactInfo(baseRevision, file);
    return new FossilRevisionNumber(baseRevision, artifactInfo.getDate());
  }

  public FossilRevisionNumber getRevisionNumber(final File file, final String revNum) throws VcsException {
    final ArtifactInfo artifactInfo = getArtifactInfo(revNum, file);
    return new FossilRevisionNumber(revNum, artifactInfo.getDate());
  }

  public String getBaseRevision(final File file) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, MoveWorker.findParent(file), FCommandName.finfo);
    command.addParameters("--limit", "1");
    command.addParameters(file.getPath());
    String result = command.run().trim();
    result = result.replace("\r", "");
    final String[] lines = result.split("\n");
    if (lines.length == 0) {
      throw new FossilException("Can not find base revision for " + file.getPath());
    }
    if (lines[0].startsWith("History of")) {
      // ok
      if (lines.length < 2) throw new FossilException("Can not find base revision for " + file.getPath() + "\n:" + result);
      final int idx1 = lines[1].indexOf('[');
      final int idx2 = lines[1].indexOf(']');
      if (idx1 == -1 || idx2 == -1 || idx1 >= idx2) {
        throw new FossilException("Can not find base revision for " + file.getPath() + "\n:" + result);
      }
      return lines[1].substring(idx1 + 1, idx2);
    } else {
      throw new FossilException(result);
    }
  }

  public ArtifactInfo getArtifactInfo(final String hash, final File file) throws VcsException {
    final FossilSimpleCommand command = new FossilSimpleCommand(myProject, MoveWorker.findParent(file), FCommandName.artifact);
    command.addParameters(hash);
    String result = command.run();
    result = result.replace("\r", "");
    final String[] split = result.split("\n");
    final Map<Character, String> map = new HashMap<Character, String>();
    for (String s : split) {
      s = s.trim();
      map.put(s.charAt(0), s.substring(2));
    }
    final ArtifactInfo artifactInfo = new ArtifactInfo();
    artifactInfo.setHash(hash);
    final String u = map.get('U');
    if (u == null) throw new FossilException("Cannot find user name in artifact output: " + result);
    artifactInfo.setUser(u);
    final String d = map.get('D');
    if (d == null) throw new FossilException("Cannot find date in artifact output: " + result);
    final Date date = DateUtil.parseDate(d);
    if (date == null) throw new FossilException("Cannot parse date in artifact output: " + d);
    artifactInfo.setDate(date);
    final String checksum = map.get('Z');
    if (checksum == null) throw new FossilException("Cannot find checksum in artifact output: " + result);
    artifactInfo.setCheckSum(checksum);
    return artifactInfo;
  }

  /*c:\fossil\test>fossil artifact 8191
  C "one\smore"
  D 2013-02-24T12:35:49.533
  F 1236.txt 40bd001563085fc35165329ea1ff5c5ecbdbbeef
  F a/aabb.txt f6190088959858b555211616ed50525a353aaaca
  F a/newFile.txt da39a3ee5e6b4b0d3255bfef95601890afd80709
  F a/text.txt da39a3ee5e6b4b0d3255bfef95601890afd80709
  P 628c7cec770e38c2c52b43aec82e194dff4384bc
  R 444f07d947464b09248dfc1f2ac4f64b
  U Irina.Chernushina
  Z 50aa202bcfcc4936e374722dcead9329

  c:\fossil\test>fossil artifact a/aabb.txt
  fossil: not found: a/aabb.txt

  c:\fossil\test>fossil artifact ./a/aabb.txt
  fossil: not found: ./a/aabb.txt

  c:\fossil\test>fossil finfo --limit 1 a/aabb.txt
  History of a/aabb.txt
  2013-02-24 [8191f07a6d] "one more" (user: Irina.Chernushina, artifact:
             [f619008895], branch: trunk)*/
}
