package org.avaje.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.avaje.freemarker.layout.InheritLayoutTemplateLoader;
import org.avaje.freemarker.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;

public class TemplateLoaderFactoryTests {

  @Test
  public void testLoadFromFile() throws IOException {

    TemplateLoader templateLoader = new FileTemplateLoader(new File("src/test/resources/templates"));
    
    Object templateSource = templateLoader.findTemplateSource("test.html");
    Assert.assertNotNull(templateSource);

    Reader reader = templateLoader.getReader(templateSource, "UTF-8");
    String content = IOUtil.read(reader);
    Assert.assertTrue(content.indexOf("<p>Some test content</p>") > -1);
  }
  
  @Test
  public void testInheritance() throws IOException {
    
    
    TemplateLoader baseLoader = new FileTemplateLoader(new File("src/test/resources/templates"));
    
    InheritLayoutTemplateLoader layoutLoader = new InheritLayoutTemplateLoader(baseLoader);

    Object mainLayoutPageSource = layoutLoader.findTemplateSource("layout/mainLayout.html");
    Assert.assertNotNull(mainLayoutPageSource);

    Object somePageSource = layoutLoader.findTemplateSource("somePage.html");
    Assert.assertNotNull(somePageSource);

    Reader reader = layoutLoader.getReader(somePageSource, "UTF-8");
    String content = IOUtil.read(reader);
    
    Assert.assertTrue(content.indexOf("<div>layout before main content</div>") > -1);
    Assert.assertTrue(content.indexOf("<div>Some Page content</div>") > -1);
    
  }
}
