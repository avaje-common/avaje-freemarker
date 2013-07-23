package org.avaje.freemarker;

import java.util.HashMap;
import java.util.Map;

/**
 * A Model and View that will be rendered.
 */
public class ModelView {

  /**
   * The name of the view.
   */
  private String view;
  
  /**
   * The model used when rendering.
   */
  private Map<String,Object> model;
  
  /**
   * Create with a view name.
   */
  public ModelView(String view) {
    this.view = view;
    this.model = new HashMap<String,Object>();
  }
  
  /**
   * Create with a view name and model.
   */
  public ModelView(String view, Map<String,Object> model) {
    this.view = view;
    this.model = model;
  }

  /**
   * Return the name of the view to render.
   */
  public String getView() {
    return view;
  }

  /**
   * Set the name of the view to render.
   */
  public void setView(String view) {
    this.view = view;
  }

  /**
   * Return the model object.
   */
  public Map<String, Object> getModel() {
    return model;
  }

  /**
   * Set the model object.
   */
  public void setModel(Map<String, Object> model) {
    this.model = model;
  }
  
  /**
   * Put something into the model.
   */
  public void put(String key, Object value) {
    model.put(key, value);
  }
  
}
