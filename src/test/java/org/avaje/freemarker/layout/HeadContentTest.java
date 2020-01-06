package org.avaje.freemarker.layout;

import org.junit.Test;

import static org.junit.Assert.*;

public class HeadContentTest {

  @Test
  public void readParentLayout() {

    String line = "<head>\n<meta name=\"layout\" content=\"_layout/main.html\"/>\n<head>";
    HeadContent content = new HeadContent(line, 0, "test");

    assertTrue(content.hasParentLayout());
    assertEquals("_layout/main.html", content.getParentLayout());
  }
}