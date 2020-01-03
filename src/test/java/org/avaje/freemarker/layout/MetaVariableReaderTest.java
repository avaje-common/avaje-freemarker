package org.avaje.freemarker.layout;

import org.avaje.freemarker.util.IOUtil;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MetaVariableReaderTest {

  @Test
  public void readTemplateVariables() throws IOException {

    Map<String, String> map = new LinkedHashMap<>();
    MetaVariableReader.readAll(read("/templates/meta/meta1.html"), map);

    assertThat(map.get("foo")).isEqualTo("Hello");
    assertThat(map.get("bar").trim()).isEqualTo("Bar");
    assertThat(map.get("notFound")).isNull();
  }

  @Test
  public void readVarVariables() throws IOException {

    Map<String, String> map = new LinkedHashMap<>();
    MetaVariableReader.readAll(read("/templates/meta/var1.html"), map);

    assertThat(map.get("foo")).isEqualTo("Hello");
    assertThat(map.get("bar").trim()).isEqualTo("Bar");
    assertThat(map.get("notFound")).isNull();
  }

  private String read(String url) throws IOException {
    return IOUtil.readUTF8(getClass().getResourceAsStream(url));
  }
}