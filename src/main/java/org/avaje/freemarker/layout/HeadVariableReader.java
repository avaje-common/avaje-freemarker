package org.avaje.freemarker.layout;

import java.util.Map;

/**
 * Reads, extracts variables from the head meta section.
 */
class HeadVariableReader {

  private final Map<String, String> variables;
  private String content;
  private int start;

  private String elementStart = "<template id=\"";
  private String elementEnd = "</template>";

  static String readAll(String content, Map<String, String> variables) {
    return new HeadVariableReader(content, variables).read();
  }

  HeadVariableReader(String content, Map<String, String> variables) {
    this.variables = variables;
    this.content = content;
  }

  private void readUsingVarVariables() {
    elementStart = "<var id=\"";
    elementEnd = "</var>";
    readVariables();
  }

  private void readPositions() {
    start = content.indexOf(elementStart);
  }

  String read() {
    readVariables();
    readUsingVarVariables();
    return content;
  }

  void readVariables() {
    readPositions();
    while (start > -1) {
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
