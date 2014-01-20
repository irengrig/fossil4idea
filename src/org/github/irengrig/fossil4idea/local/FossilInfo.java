package org.github.irengrig.fossil4idea.local;

import org.github.irengrig.fossil4idea.repository.FossilRevisionNumber;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 12:11 AM
 */
public class FossilInfo {
  private String myProjectName;
  private String myRepository;
  private String myLocalPath;
  private String myUserHome;
  private String myProjectId;
  private FossilRevisionNumber myNumber;
  private List<String> myTags;
  private String myComment;

  public FossilInfo() {
  }

  public FossilInfo(final String projectName, final String repository, final String localPath, final String userHome,
                    final String projectId, final FossilRevisionNumber number, final List<String> tags, final String comment) {
    myProjectName = projectName;
    myRepository = repository;
    myLocalPath = localPath;
    myUserHome = userHome;
    myProjectId = projectId;
    myNumber = number;
    myTags = tags;
    myComment = comment;
  }

  public String getProjectName() {
    return myProjectName;
  }

  public void setProjectName(final String projectName) {
    myProjectName = projectName;
  }

  public String getRepository() {
    return myRepository;
  }

  public void setRepository(final String repository) {
    myRepository = repository;
  }

  public String getLocalPath() {
    return myLocalPath;
  }

  public void setLocalPath(final String localPath) {
    myLocalPath = localPath;
  }

  public String getUserHome() {
    return myUserHome;
  }

  public void setUserHome(final String userHome) {
    myUserHome = userHome;
  }

  public String getProjectId() {
    return myProjectId;
  }

  public void setProjectId(final String projectId) {
    myProjectId = projectId;
  }

  public FossilRevisionNumber getNumber() {
    return myNumber;
  }

  public void setNumber(final FossilRevisionNumber number) {
    myNumber = number;
  }

  public List<String> getTags() {
    return myTags;
  }

  public void setTags(final List<String> tags) {
    myTags = tags;
  }

  public String getComment() {
    return myComment;
  }

  public void setComment(final String comment) {
    myComment = comment;
  }
}
