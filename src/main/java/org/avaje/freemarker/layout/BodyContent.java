package org.avaje.freemarker.layout;

class BodyContent implements Tags {

  private final boolean withBody;
  private String bodyTagAttributes;
  private String bodyContent;

  private BodyContent(String bodyContent) {
    this.withBody = false;
    this.bodyContent = bodyContent;
    this.bodyTagAttributes = null;
  }
  private BodyContent(String bodyContent, String bodyTagAttributes) {
    this.withBody = true;
    this.bodyContent = bodyContent;
    this.bodyTagAttributes = bodyTagAttributes;
  }

  public static BodyContent parse(String templateName, String originalContent) {
    int bodyTagStart = originalContent.indexOf("<body");
    if (bodyTagStart == -1) {
      return new BodyContent(originalContent);
    }
    int bodyTagEnd = originalContent.indexOf(_END_TAG, bodyTagStart + 5);
    if (bodyTagEnd == -1) {
      throw new RuntimeException("'>' character not found in template [" + templateName + "] after '<body' position [" + bodyTagStart + 5 + "]");
    }
    int bodyEnd = originalContent.lastIndexOf("</body>");
    if (bodyEnd == -1) {
      throw new RuntimeException("'</body>' tag not found in template [" + templateName + "] after position [" + bodyTagEnd + "]");
    }
    String bodyTagAttributes = originalContent.substring(bodyTagStart + 5, bodyTagEnd);
    String bodyContent = originalContent.substring(bodyTagEnd + 1, bodyEnd);
    return new BodyContent(bodyContent, bodyTagAttributes);
  }

  public void mergeWith(BodyContent childBody) {
    if (childBody.bodyTagAttributes != null) {
      bodyTagAttributes = childBody.bodyTagAttributes;
    }
    bodyContent = StringHelper.replaceString(bodyContent, layoutBodyTag, childBody.getBodyContent());
  }

  public String getBodyContent() {
    return bodyContent;
  }

  public void render(StringBuilder sb) {
    if (withBody) {
      sb.append("<body");
      if (bodyTagAttributes != null) {
        sb.append(bodyTagAttributes);
      }
      sb.append(">\n");
    }
    sb.append(bodyContent);
    if (withBody) {
      sb.append("\n</body>\n</html>\n");
    }
  }
}
