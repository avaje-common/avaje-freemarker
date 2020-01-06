package org.avaje.freemarker.layout;

class PageContent {

  private final String templateName;

  private final String originalContent;
  private HeadContent headContent;
  private BodyContent bodyContent;

  PageContent(String templateName, String originalContent) {
    this.templateName = templateName;
    this.originalContent = originalContent;
    parse();
  }

  boolean hasParentLayout() {
    return headContent != null && headContent.hasParentLayout();
  }

  public String getParentLayout() {
    return headContent.getParentLayout();
  }

  private void parse() {
    bodyContent = BodyContent.parse(templateName, originalContent);
    headContent = HeadContent.parse(templateName, originalContent);
  }


  String renderContent() {
		String pageContent = baseRender();
    pageContent = headContent.replaceVariables(pageContent);
    pageContent = headContent.replaceBreadCrumbs(pageContent);
    return pageContent;
  }

  private String baseRender() {
    StringBuilder sb = new StringBuilder(originalContent.length() + 512);
    int headStart = headContent.start();
    if (headStart > -1) {
			sb.append(originalContent.substring(0, headStart).trim());
			headContent.render(sb);
		}
    bodyContent.render(sb);
		return sb.toString();
	}

  void mergeChild(PageContent child) {
    this.headContent.mergeWith(child.headContent);
    this.bodyContent.mergeWith(child.bodyContent);
  }

}
