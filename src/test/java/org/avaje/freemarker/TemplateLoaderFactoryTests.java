package org.avaje.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import org.avaje.freemarker.layout.InheritLayoutTemplateLoader;
import org.avaje.freemarker.util.IOUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TemplateLoaderFactoryTests {

  @Test
  public void testLoadFromFile() throws IOException {

    TemplateLoader templateLoader = new FileTemplateLoader(new File("src/test/resources/templates"));
    
    Object templateSource = templateLoader.findTemplateSource("test.html");
    assertNotNull(templateSource);

    Reader reader = templateLoader.getReader(templateSource, "UTF-8");
    String content = IOUtil.read(reader);

    assertThat(content).contains("<p>Some test content</p>");
  }
  
  @Test
  public void testInheritance() throws IOException {


    InheritLayoutTemplateLoader layoutLoader = createLoader();

    Object mainLayoutPageSource = layoutLoader.findTemplateSource("layout/mainLayout.html");
    assertNotNull(mainLayoutPageSource);

    Object somePageSource = layoutLoader.findTemplateSource("somePage.html");
    assertNotNull(somePageSource);

    Reader reader = layoutLoader.getReader(somePageSource, "UTF-8");
    String content = IOUtil.read(reader);
    
    assertThat(content).contains("<div>layout before main content</div>");
    assertThat(content).contains("<div>Some Page content</div>");
  }

  @Test
  public void nestedInheritance() throws IOException {

    String content = read("withNestedLayout.html");
    assertExpectedContent(content);
  }



  @Test
  public void nestedInheritanceNoMetaLayout() throws IOException {

    String content = read("withNestedLayout2.html");
    assertExpectedContent(content);

    assertThat(content).contains("<nav><#include \"/_layout/_docs_nav_mapping.ftl\"></nav>");
    assertThat(content).contains("<nav><#include \"/_layout/_docs_nav_mapping.ftl\"></nav>");

    assertThat(content).contains("<h1 id=\"bread\"><a href=\"/docs\">Docs</a> / Mapping</h1>");
  }

  private void assertExpectedContent(String content) {

    assertThat(content).contains("<div>my nested page content</div>");
    assertThat(content).contains("<title>Nested</title>");
    assertThat(content).contains("<body onload=\"\">");
    assertThat(content).contains("<meta name=\"base meta before\"/>");
    assertThat(content).contains("<meta name=\"parent meta before\"/>");

    assertThat(content).contains("<div>parent before main content</div>");
    assertThat(content).contains("<div>nested before main content</div>");
    assertThat(content).contains("<div>nested after main content</div>");
    assertThat(content).contains("<div>parent after main content</div>");

    assertThat(content).doesNotContain("<div id=\"meta-content-body\"></div>");

    assertThat(content).contains("<div class=\"meta\"><a href=\"foo\">metContent</a></div>");
  }

  private String read(String templateName) throws IOException {

    InheritLayoutTemplateLoader layoutLoader = createLoader();
    Object somePageSource = layoutLoader.findTemplateSource(templateName);
    Reader reader = layoutLoader.getReader(somePageSource, "UTF-8");
    return IOUtil.read(reader);
  }

  private InheritLayoutTemplateLoader createLoader() throws IOException {
    TemplateLoader baseLoader = new FileTemplateLoader(new File("src/test/resources/templates"));
    return new InheritLayoutTemplateLoader(baseLoader, null);
  }

}
