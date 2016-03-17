package org.avaje.freemarker;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import org.avaje.freemarker.layout.InheritLayoutTemplateLoader;
import org.avaje.freemarker.util.IOUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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
    assertTrue(content.indexOf("<p>Some test content</p>") > -1);
  }
  
  @Test
  public void testInheritance() throws IOException {
    
    
    TemplateLoader baseLoader = new FileTemplateLoader(new File("src/test/resources/templates"));
    
    InheritLayoutTemplateLoader layoutLoader = new InheritLayoutTemplateLoader(baseLoader, null);

    Object mainLayoutPageSource = layoutLoader.findTemplateSource("layout/mainLayout.html");
    assertNotNull(mainLayoutPageSource);

    Object somePageSource = layoutLoader.findTemplateSource("somePage.html");
    assertNotNull(somePageSource);

    Reader reader = layoutLoader.getReader(somePageSource, "UTF-8");
    String content = IOUtil.read(reader);
    
    assertTrue(content.indexOf("<div>layout before main content</div>") > -1);
    assertTrue(content.indexOf("<div>Some Page content</div>") > -1);
    
  }

  @Test
  public void nestedInheritance() throws IOException {


    TemplateLoader baseLoader = new FileTemplateLoader(new File("src/test/resources/templates"));

    InheritLayoutTemplateLoader layoutLoader = new InheritLayoutTemplateLoader(baseLoader, null);

    Object somePageSource = layoutLoader.findTemplateSource("withNestedLayout.html");
    assertNotNull(somePageSource);

    Reader reader = layoutLoader.getReader(somePageSource, "UTF-8");
    String content = IOUtil.read(reader);

    assertTrue(content.indexOf("<div>layout before main content</div>") > -1);
    assertTrue(content.indexOf("<div>Some Page content</div>") > -1);
  }
}
