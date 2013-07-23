package org.avaje.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateExceptionHandler;

/**
 * Bean used to build a Freemarker Configuration object.
 */
public class ConfigurationBuilder {

  private ObjectWrapper wrapper = ObjectWrapper.DEFAULT_WRAPPER;

  private String encoding = "UTF-8";

  private boolean localizedLookup;
  
  private int templateUpdateDelay;

  private boolean useExceptionHandler;

  private TemplateLoader templateLoader;

  public Configuration build() {

    try {
      Configuration config = new Configuration();
      config.setObjectWrapper(wrapper);
      config.setLocalizedLookup(localizedLookup);
      config.setDefaultEncoding(encoding);
      config.setTemplateUpdateDelay(templateUpdateDelay);

      if (useExceptionHandler) {
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      }

      config.setTemplateLoader(templateLoader);

      return config;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public ObjectWrapper getWrapper() {
    return wrapper;
  }

  public void setWrapper(ObjectWrapper wrapper) {
    this.wrapper = wrapper;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public int getTemplateUpdateDelay() {
    return templateUpdateDelay;
  }

  public void setTemplateUpdateDelay(int templateUpdateDelay) {
    this.templateUpdateDelay = templateUpdateDelay;
  }

  public boolean isUseExceptionHandler() {
    return useExceptionHandler;
  }

  public void setUseExceptionHandler(boolean useExceptionHandler) {
    this.useExceptionHandler = useExceptionHandler;
  }

  public boolean isLocalizedLookup() {
    return localizedLookup;
  }

  public void setLocalizedLookup(boolean localizedLookup) {
    this.localizedLookup = localizedLookup;
  }

  public TemplateLoader getTemplateLoader() {
    return templateLoader;
  }

  public void setTemplateLoader(TemplateLoader templateLoader) {
    this.templateLoader = templateLoader;
  }
  
}
