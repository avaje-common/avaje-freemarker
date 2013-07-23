package org.avaje.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.avaje.freemarker.layout.InheritLayoutTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Service that provides freemarker template rendering.
 */
@Component
public class TemplateService {

  private static final Logger log = LoggerFactory.getLogger(TemplateService.class);
  
  private final ServletContext servletContext;
    
  private final Configuration configuration;
  
  private final String viewSuffix;
  
  /**
   * Constructs the service with appropriate template loading.
   */
  @Inject
  public TemplateService(WebApplicationContext webAppContext, TemplateConfig config) {
      
    this.servletContext = webAppContext.getServletContext();
    this.viewSuffix = config.getViewSuffix();


    ConfigurationBuilder builder = new ConfigurationBuilder();
    
    boolean devMode = config.isDevMode();
    int updateDelay = config.getUpdateDelay();
    String templatePath = config.getTemplatePath();
    
    log.info("templates devMode:{} updateDelay:{} templatePath:{}", devMode, updateDelay, templatePath);
    
    builder.setUseExceptionHandler(devMode);
    if (!devMode) {
      builder.setTemplateUpdateDelay(updateDelay);
    }    
    
    builder.setTemplateLoader(createTemplateLoader(templatePath));
    
    this.configuration = builder.build();  
  }

  private TemplateLoader createTemplateLoader(String templatePath) {
    
    log.debug("templatePath: {}",templatePath);    
    return InheritLayoutTemplateLoader.createWebappLoader(servletContext, templatePath);
  }


  /**
   * Render the template with the given model to the writer.
   */
  public void render(String templateName, Map<?,?> model, Writer writer) throws IOException {
    
    Template template;
    try {
      template = configuration.getTemplate(templateName+viewSuffix);
    } catch (IOException e) {
      throw new IOException("Error loading template: "+templateName, e);
    }
    
    try {
      SimpleHash wrappedModel = new SimpleHash(model);
      template.process(wrappedModel, writer);

    } catch (TemplateException e) {
      log.error("Error processing template: "+templateName, e);
    }
  }
  
}
