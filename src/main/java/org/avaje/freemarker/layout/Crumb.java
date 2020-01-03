package org.avaje.freemarker.layout;

class Crumb {

  String name;
  String desc;
  String href;

  Crumb(String name, String desc, String href) {
    this.name = name;
    this.desc = desc;
    this.href = href;
  }

  String render(boolean withLink) {
    if (withLink) {
      return "<a href=\"" + href + "\">" + desc + "</a>";
    } else {
      return "<span class=\"last\">" + desc + "</span>";
    }
  }

  public String toString() {
    return name + " " + desc + " " + href;
  }
}
