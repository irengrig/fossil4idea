package org.jetbrains.fossil.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.annotate.AnnotationSourceSwitcher;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspectAdapter;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.fossil.repository.FossilRevisionNumber;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 9:14 PM
 */
public class FossilFileAnnotation extends FileAnnotation {
  final Map<Integer, ArtifactInfo> myMap;
  private int myMaxIdx;
  private final Project myProject;
  private final String myContent;
  private final FossilRevisionNumber myNumber;
  private final VirtualFile myVirtualFile;

  public FossilFileAnnotation(final Project project, final String content, final FossilRevisionNumber number,
                              final VirtualFile virtualFile) {
    super(project);
    myProject = project;
    myContent = content;
    myNumber = number;
    myVirtualFile = virtualFile;
    myMap = new HashMap<Integer, ArtifactInfo>();
    myMaxIdx = 0;
  }

  public void registerLine(final int number, final ArtifactInfo info) {
    myMap.put(number, info);
    myMaxIdx = Math.max(number, myMaxIdx);
  }

  private LineAnnotationAspect DATE = new MyAnnotationAspect(LineAnnotationAspect.DATE) {
    @Override
    public String getValue(final int line) {
      if (line < 0 || line > myMaxIdx) return "";
      final ArtifactInfo artifactInfo = myMap.get(line);
      return artifactInfo == null ? "" : DateFormatUtil.formatDate(artifactInfo.getDate());
    }
  };
  private LineAnnotationAspect REVISION = new MyAnnotationAspect(LineAnnotationAspect.REVISION) {
    @Override
    public String getValue(final int line) {
      if (line < 0 || line > myMaxIdx) return "";
      final ArtifactInfo artifactInfo = myMap.get(line);
      String hash = artifactInfo.getHash();
      hash = hash.length() > 8 ? hash.substring(0, 8) : hash;
      return artifactInfo == null ? "" : hash;
    }
  };
  private LineAnnotationAspect AUTHOR = new MyAnnotationAspect(LineAnnotationAspect.AUTHOR) {
    @Override
    public String getValue(final int line) {
      if (line < 0 || line > myMaxIdx) return "";
      final ArtifactInfo artifactInfo = myMap.get(line);
      return artifactInfo == null ? "" : artifactInfo.getUser();
    }
  };

  @Override
  public void dispose() {
  }

  @Override
  public LineAnnotationAspect[] getAspects() {
    return new LineAnnotationAspect[]{REVISION, DATE, AUTHOR};
  }

  @Nullable
  @Override
  public String getToolTip(final int line) {
    if (line < 0 || line > myMaxIdx) return "";
    final ArtifactInfo artifactInfo = myMap.get(line);
    return artifactInfo == null ? "" : artifactInfo.getComment();
  }

  @Override
  public String getAnnotatedContent() {
    return myContent;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getLineRevisionNumber(final int line) {
    if (line < 0 || line > myMaxIdx) return null;
    final ArtifactInfo artifactInfo = myMap.get(line);
    if (artifactInfo == null) return null;
    return new FossilRevisionNumber(artifactInfo.getHash(), artifactInfo.getDate());
  }

  @Nullable
  @Override
  public Date getLineDate(final int line) {
    if (line < 0 || line > myMaxIdx) return null;
    final ArtifactInfo artifactInfo = myMap.get(line);
    if (artifactInfo == null) return null;
    return artifactInfo.getDate();
  }

  @Nullable
  @Override
  public VcsRevisionNumber originalRevision(final int lineNumber) {
    return getLineRevisionNumber(lineNumber);
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevision() {
    return myNumber;
  }

  @Nullable
  @Override
  public List<VcsFileRevision> getRevisions() {
    final HashSet<ArtifactInfo> artifactInfos = new HashSet<ArtifactInfo>(myMap.values());
    final List<VcsFileRevision> result = new ArrayList<VcsFileRevision>(myMap.size());
    //todo correct filepath
    final FilePath fp = new FilePathImpl(myVirtualFile);
    for (ArtifactInfo artifactInfo : artifactInfos) {
      result.add(new FossilFileRevision(myProject, fp, new FossilRevisionNumber(artifactInfo.getHash(), artifactInfo.getDate()),
          artifactInfo.getUser(), artifactInfo.getComment()));
    }
    Collections.sort(result, new Comparator<VcsFileRevision>() {
      @Override
      public int compare(final VcsFileRevision o1, final VcsFileRevision o2) {
        return o1.getRevisionDate().compareTo(o2.getRevisionDate());
      }
    });
    return result;
  }

  @Override
  public boolean revisionsNotEmpty() {
    return ! myMap.isEmpty();
  }

  @Nullable
  @Override
  public AnnotationSourceSwitcher getAnnotationSourceSwitcher() {
    return null;
  }

  @Override
  public int getLineCount() {
    return myMaxIdx + 1;///todo think
  }

  @Override
  public VirtualFile getFile() {
    return myVirtualFile;
  }

  private abstract class MyAnnotationAspect extends LineAnnotationAspectAdapter {
    protected MyAnnotationAspect(final String id) {
      super(id);
    }

    @Override
    public boolean isShowByDefault() {
      return true;
    }

    @Override
    public String getTooltipText(final int line) {
      if (line < 0 || line > myMaxIdx) return null;
      final ArtifactInfo artifactInfo = myMap.get(line);
      if (artifactInfo == null) return null;
      return artifactInfo.getComment();
    }

    @Override
    protected void showAffectedPaths(final int line) {
      // todo
    }
  }
}
