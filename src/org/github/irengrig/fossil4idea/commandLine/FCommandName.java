package org.github.irengrig.fossil4idea.commandLine;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/13/13
 * Time: 8:08 PM
 */
public enum FCommandName {
  add("add"),
  addremove("addremove"),
  all_("all"),
  annotate("annotate"),
  artifact("artifact"),
  bisect("bisect"),
  branch("branch"),
  cgi("cgi"),
  changes("changes"),
  checkout("checkout"),
  ci("ci"),
  clean("clean"),
  clone("clone"),
  close("close"),
  co("co"),
  commit("commit"),
  configuration("configuration"),
  deconstruct("deconstruct"),
  delete("delete"),
  descendants("descendants"),
  diff("diff"),
  export("export"),
  extras("extras"),
  finfo("finfo"),
  gdiff("gdiff"),
  http_("http"),
  import_("import"),
  info("info"),
  init("init"),
  leaves("leaves"),
  ls("ls"),
  md5sum("md5sum"),
  merge("merge"),
  mv("mv"),
  new_("new"),
  open("open"),
  pull("pull"),
  push("push"),
  rebuild("rebuild"),
  reconstruct("reconstruct"),
  redo("redo"),
  remote_url("remote-url"),
  rename("rename"),
  revert("revert"),
  rm("rm"),
  scrub("scrub"),
  search("search"),
  server("server"),
  settings("settings"),
  sha1sum("sha1sum"),
  sqlite3("sqlite3"),
  stash("stash"),
  status("status"),
  synch("synch"),
  tag("tag"),
  tarball("tarball"),
  ticket("ticket"),
  timeline("timeline"),
  ui("ui"),
  undo("undo"),
  unset("unset"),
  update("update"),
  user("user"),
  version("version"),
  whatis("whatis"),
  wiki("wiki"),
  winsrv("winsrv"),
  zip("zip");

  private final String myName;

  private FCommandName(String name) {
    myName = name;
  }

  public String getName() {
    return myName;
  }
}
