package org.avaje.freemarker;

/**
 * Provides configuration for the template rendering.
 */
public interface TemplateConfig {

  /**
   * Return true if the server is in DEV mode.
   */
  public boolean isDevMode();
  
  /**
   * Return the default view suffix. 
   * <p>
   * A reasonable value for this is ".html"
   * </p>
   */
  public String getViewSuffix();
  
  /**
   * Return the delay in seconds used to detect updates on the templates.
   */
  public int getUpdateDelay();
  
  /**
   * Return the path where the templates are found.
   * <p>
   * A reasonable value for this is "views" or "templates". 
   * </p>
   */
  public String getTemplatePath();
}
