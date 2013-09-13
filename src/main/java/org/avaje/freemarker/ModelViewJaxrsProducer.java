package org.avaje.freemarker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A JAX-RS Producer for rendering Freemarker ModelView objects.
 */
@Component
@Provider
@Produces({ MediaType.TEXT_HTML, "text/html" })
public class ModelViewJaxrsProducer implements MessageBodyWriter<ModelView> {

  private static final Logger log = LoggerFactory.getLogger(ModelViewJaxrsProducer.class);

  /**
   * Service that renders the Freemarker templates 
   */
  private final TemplateService templateService;
  
  @Inject
  public ModelViewJaxrsProducer(TemplateService templateService) {
    this.templateService = templateService;
  }

  public long getSize(ModelView modelView, Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  public boolean isWriteable(Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType) {

    return ModelView.class.isAssignableFrom(cls);
  }

  /**
   * Renders the ModelView to the OutputStream.
   */
  public void writeTo(ModelView modelView, Class<?> cls, Type type, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream os) throws IOException, WebApplicationException {

    try {

      Writer writer = new BufferedWriter(new OutputStreamWriter(os));

      templateService.render(modelView.getView(), modelView.getModel(), writer);
      writer.flush();

    } catch (IOException e) {
      log.error("Template not found: "+e.getMessage());
      
      ModelView notFoundView = templateService.buildNotFound(modelView.getView());
      
      Response response = Response.status(Status.NOT_FOUND)
          .entity(notFoundView).type(MediaType.TEXT_HTML).build();
      
      throw new WebApplicationException(response);
      
    } catch (Exception e) {
      log.error("Error rendering template", e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
   
  }

}
