package org.avaje.freemarker;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PageNotFoundException extends WebApplicationException {
  public PageNotFoundException(String message) {
    super(Response.status(Response.Status.BAD_REQUEST)
        .entity(message).type(MediaType.TEXT_HTML).build());
  }
}