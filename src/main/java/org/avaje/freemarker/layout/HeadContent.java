package org.avaje.freemarker.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class HeadContent implements Tags {

  public static final HeadContent NONE = new HeadContent();

  private static final Logger log = LoggerFactory.getLogger(HeadContent.class);

  private static final String TAG_TITLE = "<title>";
  private static final String TAG_TITLE_END = "</title>";
  private static final String META_LAYOUT = "<meta name=\"layout\"";
  private static final String _END_TAG = ">";
  private static final String TAG_BREAD = "<meta name=\"bread";
  private static final String META_ID_LAYOUT_HEAD = "<meta id=\"layout-head\"/>";

  private List<String> lines = new ArrayList<>();
  private Map<String, String> variables = new LinkedHashMap<>();
  private Map<String, Crumb> bread = new LinkedHashMap<>();

  private final int headStart;
  private String content;
  private String title;
  private String parentLayout;
  //  private String templateMetaContent;
  private int metaAddPosition = -1;

  private HeadContent() {
    this.headStart = -1;
  }

  public HeadContent(String content, int headStart) {
    this.headStart = headStart;
    this.content = content;
    parseMetaContent();
    parseLines();
  }

  public int start() {
    return headStart;
  }

  public void render(StringBuilder sb) {
    sb.append("\n<head>\n");
    if (title != null) {
      sb.append(String.format("  <title>%s</title>\n", title));
    }
    for (String line : lines) {
      if (!line.trim().isEmpty()) {
        sb.append(line).append("\n");
      }
    }
    sb.append("</head>\n");
  }

  static HeadContent parse(String templateName, String originalContent) {
    int headStart = originalContent.indexOf(_HEAD);
    if (headStart == -1) {
      return HeadContent.NONE;
    } else {
      int headEnd = originalContent.indexOf(_HEAD_END, headStart + _HEAD.length());
      if (headEnd == -1) {
        throw new RuntimeException("'" + _HEAD_END + "' tag not found in template[" + templateName + "] after position [" + headStart + "]");
      }
      return new HeadContent(originalContent.substring(headStart + 6, headEnd), headStart);
    }
  }

  boolean hasParentLayout() {
    return parentLayout != null;
  }

  String getParentLayout() {
    return parentLayout;
  }

  private void parseLines() {
    LineNumberReader lineReader = new LineNumberReader(new StringReader(content));
    try {
      String line;
      while ((line = lineReader.readLine()) != null) {
        addLine(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addLine(String line) {
    boolean consumed =
      readTitle(line)
        || readParentLayout(line)
        || readCrumb(line)
        || readMetaLayout(line);

    if (!consumed) {
      lines.add(line);
    }
  }

  private boolean readMetaLayout(String line) {
    if (!line.contains(META_ID_LAYOUT_HEAD)) {
      return false;
    }
    metaAddPosition = lines.size();
    return true;
  }

  private boolean readParentLayout(String line) {
    if (parentLayout != null) {
      return false;
    }
    final int pos = line.indexOf(META_LAYOUT);
    if (pos == -1) {
      return false;
    }
    int end = line.lastIndexOf(_END_TAG);
    if (end == -1) {
      throw new RuntimeException("'>' not found for <meta name=\"content\" in line [" + line + "]");
    }
    int tnStart = line.indexOf("content=", pos);
    if (tnStart == -1) {
      throw new RuntimeException("No 'content=' attribute in <meta name=\"layout\" ... element?");
    }
    String layout = line.substring(tnStart + 8, end);
    if (layout.endsWith("/")) {
      layout = layout.substring(0, layout.length() - 1);
    }
    parentLayout = StringHelper.removeChars(layout, new char[]{'"', '\''}).trim();
    return true;
  }

  private boolean readTitle(String line) {
    if (title != null) {
      return false;
    }
    final int pos = line.indexOf(TAG_TITLE);
    if (pos == -1) {
      return false;
    }
    final int end = line.lastIndexOf(TAG_TITLE_END);
    title = line.substring(pos + TAG_TITLE.length(), end);
    return true;
  }

  private boolean readCrumb(String line) {
    final int pos = line.indexOf(TAG_BREAD);
    if (pos == -1) {
      return false;
    }
    int end = line.lastIndexOf("/>");
    if (end == -1) {
      throw new RuntimeException("'/>' not found in line [" + line + "]");
    }

    String breadContent = line.substring(pos, end);

    String name = getAttribute("name", breadContent);
    String desc = getAttribute("content", breadContent);
    String href = getAttribute("href", breadContent);

    Crumb crumb = new Crumb(name, desc, href);
    bread.putIfAbsent(name, crumb);
    return true;
  }


  private void parseMetaContent() {
//    int metaContentStart = content.indexOf("<template name=\"meta-content\">");
//    if (metaContentStart > -1) {
//      int metaContentEnd = content.indexOf("</template>", metaContentStart + 32);
//      if (metaContentEnd == -1) {
//        throw new RuntimeException("'</template>' not found for <template name=\"meta-content\" at pos[" + metaContentStart + 32 + "]");
//      }
//      templateMetaContent = content.substring(metaContentStart + 32, metaContentEnd).trim();
//      content = content.substring(0, metaContentStart) + content.substring(metaContentEnd + 11);
//    }
    parseVariables();
  }

  private void parseVariables() {
    content = MetaVariableReader.readAll(content, variables);
  }

  private String getAttribute(String attribute, String breadContent) {
    String attr = attribute + "=\"";
    int start = breadContent.indexOf(attr);
    int end = breadContent.indexOf("\"", start + attr.length() + 1);
    return breadContent.substring(start + attr.length(), end);
  }

  void mergeWith(HeadContent child) {
    this.variables.putAll(child.variables);
    this.bread.putAll(child.bread);
    if (child.title != null) {
      this.title = child.title;
    }
    if (metaAddPosition == -1) {
      this.lines.addAll(child.lines);
    } else {
      this.lines.addAll(metaAddPosition, child.lines);
    }
  }

  String replaceVariables(String pageContent) {
    if (variables.isEmpty()) {
      return pageContent;
    }
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      String varId = entry.getKey();
      String varContent = entry.getValue();
      pageContent = StringHelper.replaceString(pageContent, "$" + varId, varContent);
    }
    return pageContent;
  }

  String replaceBreadCrumbs(String pageContent) {
    if (bread.isEmpty()) {
      return StringHelper.replaceString(pageContent, "$breadcrumb", "");
    }

    log.info("content render ... crumbs {}", bread);
    final int size = bread.size();
    StringBuilder crumbs = new StringBuilder();
    for (int i = 0; i < size; i++) {
      Crumb crumb = bread.get("bread" + i);
      if (crumb != null) {
        boolean withLink = i < size - 1;
        String crumbContent = crumb.render(withLink);
        if (i > 0) {
          crumbs.append("<span class=\"sep\">&nbsp;/&nbsp;</span>");
        }
        crumbs.append(crumbContent);
      }
    }
    return StringHelper.replaceString(pageContent, "$breadcrumb", crumbs.toString());
  }

}
