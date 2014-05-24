package org.avaje.freemarker.layout;

/**
 * A filter that can be applied to the content after layout inheritance etc.
 */
public interface ContentFilter {

  /**
   * Apply a filter to the content.
   */
  public String filter(String content);
  
}
