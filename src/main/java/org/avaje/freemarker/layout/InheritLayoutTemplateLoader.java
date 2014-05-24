package org.avaje.freemarker.layout;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.ServletContext;

import org.avaje.freemarker.util.IOUtil;

import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;

/**
 * Simple layout inheritance wrapper for a TemplateLoader.
 * <p>
 * 
 * </p>
 */
public final class InheritLayoutTemplateLoader implements TemplateLoader {
  
  private final TemplateLoader wrapped;

  private final RawTemplateInherit inheritHandler;

  /**
   * Create a WebappTemplateLoader loader and wrap it with inheritance layout handling.
   */
  public static TemplateLoader createWebappLoader(ServletContext servletContext, String templatePath) {
    return createWebappLoader(servletContext, templatePath, null);
  }
  
  /**
   * Create a TemplateLoader additionally specifying a ContentFilter. 
   */
  public static TemplateLoader createWebappLoader(ServletContext servletContext, String templatePath, ContentFilter contentFilter) {
    return wrap(new WebappTemplateLoader(servletContext, templatePath), contentFilter);
  }
  
  /**
   * Wrap a TemplateLoader with inheritance layout handling.
   */
  public static TemplateLoader wrap(TemplateLoader baseLoader, ContentFilter contentFilter) {
    return new InheritLayoutTemplateLoader(baseLoader, contentFilter);
  }
  
  /**
   * Create wrapping a TemplateLoader.
   */
  public InheritLayoutTemplateLoader(TemplateLoader wrapped, ContentFilter contentFilter) {
    this.wrapped = wrapped;
    this.inheritHandler = new RawTemplateInherit(new Source(wrapped), contentFilter);
  }

  public Object findTemplateSource(String name) throws IOException {
    Object o = wrapped.findTemplateSource(name);
    if (o == null) {
      return null;
    }
    return new SourceWrapper(name, o);
  }

  public long getLastModified(Object templateSource) {
    return wrapped.getLastModified(((SourceWrapper) templateSource).wrappedSource);
  }

  public void closeTemplateSource(Object templateSource) throws IOException {
    wrapped.closeTemplateSource(((SourceWrapper) templateSource).wrappedSource);
  }

  public Reader getReader(Object templateSource, String encoding) throws IOException {

    return inheritHandler.getReader(((SourceWrapper) templateSource).templateName, encoding);
  }

  private static final class SourceWrapper {
    final String templateName;
    final Object wrappedSource;

    private SourceWrapper(String templateName, Object wrappedSource) {
      this.templateName = templateName;
      this.wrappedSource = wrappedSource;
    }
  }

  private static class Source implements RawTemplateSource {

    private final TemplateLoader wrapped;

    Source(TemplateLoader wrapped) {
      this.wrapped = wrapped;
    }
    
    public String getSource(String templateName, String encoding) throws IOException {

      try {
        Object s = wrapped.findTemplateSource(templateName);
        Reader reader = wrapped.getReader(s, encoding);
        return IOUtil.read(reader);
      } catch (Exception e) {
        throw new IOException("Error loading source for template: "+templateName, e);
      }
    }
  }

}
