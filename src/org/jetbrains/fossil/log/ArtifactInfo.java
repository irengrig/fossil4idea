package org.jetbrains.fossil.log;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 5:09 PM
 */
public class ArtifactInfo {
  private String myHash;
  private Date myDate;
  private String myUser;
  private String myCheckSum;

  public ArtifactInfo() {
  }

  public String getHash() {
    return myHash;
  }

  public void setHash(final String hash) {
    myHash = hash;
  }

  public Date getDate() {
    return myDate;
  }

  public void setDate(final Date date) {
    myDate = date;
  }

  public String getUser() {
    return myUser;
  }

  public void setUser(final String user) {
    myUser = user;
  }

  public String getCheckSum() {
    return myCheckSum;
  }

  public void setCheckSum(final String checkSum) {
    myCheckSum = checkSum;
  }

  /*  /*c:\fossil\test>fossil artifact 8191
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

    D time-and-date-stamp
    T (+|-|*)tag-name artifact-id ?value?
    U user-name
    Z checksum
  */
}
