package org.avaje.freemarker.layout;

import java.util.Map;

/**
 * Reads, extracts variables from the head meta section.
 */
class MetaVariableReader {

  private final Map<String, String> variables;
  private String content;
  private int endHead;
  private int start;

  private String elementStart = "<template id=\"";
  private String elementEnd = "</template>";

  static String readAll(String content, Map<String, String> variables) {
    return new MetaVariableReader(content, variables).read();
  }

  MetaVariableReader(String content, Map<String, String> variables) {
    this.variables = variables;
    this.content = content;
  }

  private void readUsingVarVariables() {
    elementStart = "<var id=\"";
    elementEnd = "</var>";
    readVariables();
  }

  private void readPositions() {
    endHead = content.indexOf("</head>");
    start = content.indexOf(elementStart);
  }

  String read() {
    readVariables();
    readUsingVarVariables();
    return content;
  }

  void readVariables() {
    readPositions();
    while (endHead > -1 && start > -1 && start < endHead) {
      readVariable(start);
      readPositions();
    }
  }

  private void readVariable(int start) {
    int pos = content.indexOf("\">", start + elementStart.length());
    if (pos == -1) {
      throw new RuntimeException("No closing '\">' reading meta variable at pos[" + start + "]");
    }
    int end = content.indexOf(elementEnd, pos);
    if (end == -1) {
      throw new RuntimeException(elementEnd + " not found reading meta variable at pos[" + pos + "]");
    }
    String varId = content.substring(start + elementStart.length(), pos).trim();
    String varContent = content.substring(pos + 2, end).trim();

    variables.putIfAbsent(varId, varContent);
    content = content.substring(0, start) + content.substring(end + elementEnd.length());
  }
}
