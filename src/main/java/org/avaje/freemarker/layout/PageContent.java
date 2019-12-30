package org.avaje.freemarker.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

class PageContent {

	private static final Logger log = LoggerFactory.getLogger(PageContent.class);

	private static final String _HEAD = "<head>";
	private static final String _END_TAG = ">";
	private static final String _HEAD_END = "</head>";
	private static final String layoutHeadTag = "<meta id=\"layout-head\"/>";
	private static final String layoutBodyTag = "<div id=\"layout-body\"></div>";
	private static final String metaLayoutBodyTag = "<div id=\"meta-content\"></div>";
	private static final String breadStartTag = "<meta name=\"bread";

	private final String templateName;
	private Map<String, String> variables = new LinkedHashMap<>();
	private Map<String, Crumb> bread = new LinkedHashMap<>();

	private int headEnd;
	private int metaLayoutStart = -1;
	private int metaLayoutEnd;
	private int titleStart;
	private int titleEnd;

	private String content;
	private String headContent;
	private String titleTagContent;
	private String bodyTagAttributes;
	private String bodyContent;
	private String parentLayout;
	//private String headTagContent;
	private String metaContent;

	PageContent(String templateName, String content) {
		this.templateName = templateName;
		this.content = content;
		parse();
	}

	boolean hasParentLayout() {
		return metaLayoutStart != -1;
	}

	public String getParentLayout() {
		return parentLayout;
	}

	private void parse() {
		int headStart = content.indexOf(_HEAD);
		if (headStart == -1) {
			return;
		}
		headEnd = content.indexOf(_HEAD_END, headStart + _HEAD.length());
		if (headEnd == -1) {
			throw new RuntimeException("'" + _HEAD_END + "' tag not found after position [" + headStart + "]");
		}
		headContent = content.substring(headStart + 6, headEnd);
		metaLayoutStart = headContent.indexOf("<meta name=\"layout\"");
		parseTitle();
		if (metaLayoutStart > -1) {
			metaLayoutEnd = headContent.indexOf(_END_TAG, metaLayoutStart + 13);
			if (metaLayoutEnd == -1) {
				throw new RuntimeException("'>' not found for <meta name=\"content\" at pos[" + metaLayoutStart + 13 + "]");
			}
			parseParentLayout();
			parseBody();
		}
	}

	private void parseBody() {

		int bodyTagStart = content.indexOf("<body", headEnd + 7);
		if (bodyTagStart == -1) {
			throw new RuntimeException("'<body' tag not found after position [" + headEnd + 7 + "]");
		}
		int bodyTagEnd = content.indexOf(_END_TAG, bodyTagStart + 5);
		if (bodyTagEnd == -1) {
			throw new RuntimeException("'>' character not found after '<body' position [" + bodyTagStart + 5 + "]");
		}
		int bodyEnd = content.lastIndexOf("</body>");
		if (bodyEnd == -1) {
			throw new RuntimeException("'</body>' tag not found after position [" + bodyTagEnd + "]");
		}

		// get the body tag content
		bodyTagAttributes = content.substring(bodyTagStart + 5, bodyTagEnd);
		bodyContent = content.substring(bodyTagEnd + 1, bodyEnd);
		removeTitleElement();
	}

	private void parseParentLayout() {
		int tnStart = headContent.indexOf("content=", metaLayoutStart);
		if (tnStart == -1) {
			throw new RuntimeException("No 'content=' attribute in <meta name=\"layout\" ... element?");
		}
		String layout = headContent.substring(tnStart + 8, metaLayoutEnd);
		if (layout.endsWith("/")) {
			layout = layout.substring(0, layout.length() - 1);
		}
		layout = StringHelper.removeChars(layout, new char[]{'"', '\''});
		parentLayout = layout.trim();
		remoteMetaLayoutElement();
	}

	private void remoteMetaLayoutElement() {
		headContent = headContent.substring(0, metaLayoutStart) + headContent.substring(metaLayoutEnd + 1);
	}

	private void removeTitleElement() {
		if (titleStart > -1) {
			headContent = headContent.substring(0, titleStart) + headContent.substring(titleEnd + 8);
		}
	}

	String content() {
		contentReplaceMetaContent();
		contentReplaceVariables();
		contentReplaceBreadCrumbs();
		return content;
	}

	private void contentReplaceMetaContent() {
		if (metaContent != null) {
			content = StringHelper.replaceString(content, metaLayoutBodyTag, metaContent);
		}
	}

	private void contentReplaceVariables() {
		if (!variables.isEmpty()) {
			for (Map.Entry<String, String> entry : variables.entrySet()) {
				String varId = entry.getKey();
				String varContent = entry.getValue();
				content = StringHelper.replaceString(content, "$" + varId, varContent);
			}
		}
	}

	private void contentReplaceBreadCrumbs() {
		int size = bread.size();
		if (size == 0) {
			content = StringHelper.replaceString(content, "$breadcrumb", "");
		} else {
			log.info("content render ... crumbs {}", bread);
			StringBuilder crumbs = new StringBuilder();
			for (int i = 0; i < size; i++) {
				Crumb crumb = bread.get("bread" + i);
				if (crumb != null) {
					boolean isAnchor = i < size - 1;
					String crumbContent = crumb.render(isAnchor);
					if (i > 0) {
						crumbs.append(" / ");
					}
					crumbs.append(crumbContent);
				}
			}
			content = StringHelper.replaceString(content, "$breadcrumb", crumbs.toString());
		}
	}

	private void parseTitle() {
		titleStart = headContent.indexOf("<title>");
		if (titleStart > -1) {
			titleEnd = headContent.indexOf("</title>", titleStart);
			if (titleEnd == -1) {
				throw new RuntimeException("'</title>' tag not found after position [" + titleStart + "]");
			}
			if (titleTagContent == null) {
				titleTagContent = headContent.substring(titleStart + 7, titleEnd);
			}
		}
		parseMetaContent();
	}

	private void parseMetaContent() {
		int metaContentStart = headContent.indexOf("<template name=\"meta-content\">");
		if (metaContentStart > -1) {
			int metaContentEnd = headContent.indexOf("</template>", metaContentStart + 32);
			if (metaContentEnd == -1) {
				throw new RuntimeException("'</template>' not found for <template name=\"meta-content\" at pos[" + metaContentStart + 32 + "]");
			}
			metaContent = headContent.substring(metaContentStart + 32, metaContentEnd).trim();
			headContent = headContent.substring(0, metaContentStart) + headContent.substring(metaContentEnd + 11);
		}
		parseVariables();
		parseBreadcrumbs();
	}

	private void parseVariables() {
		int start = headContent.indexOf("<template id=\"");
		while (start > -1) {
			start = readVariable(start);
		}
	}

	private int readVariable(int start) {
		int pos = headContent.indexOf("\">", start + 14);
		if (pos == -1) {
			throw new RuntimeException("No closing '\">' for <template id= at pos[" + start + "]");
		}
		int end = headContent.indexOf("</template>", pos);
		if (end == -1) {
			throw new RuntimeException("'</template>' not found for <template id= at pos[" + pos + "]");
		}
		String varId = headContent.substring(start + 14, pos).trim();
		String varContent = headContent.substring(pos + 2, end).trim();

		variables.putIfAbsent(varId, varContent);
		log.debug("put variable ... {} {}", varId, varContent);

		StringBuilder sb = new StringBuilder(headContent.length());
		sb.append(headContent, 0, start);
		sb.append(headContent.substring(end + 11));
		headContent = sb.toString();

		return headContent.indexOf("<template id=\"", start);
	}

	private void parseBreadcrumbs() {
		int start = headContent.indexOf(breadStartTag);
		while (start > -1) {
			start = readBread(start);
		}
	}

	private int readBread(int start) {
		int end = headContent.indexOf("/>", start);
		if (end == -1) {
			throw new RuntimeException("'/>' not found for " + breadStartTag + " at pos[" + start + "]");
		}

		String breadContent = headContent.substring(start, end);

		String name = getAttribute("name", breadContent);
		String desc = getAttribute("content", breadContent);
		String href = getAttribute("href", breadContent);

		Crumb crumb = new Crumb(name, desc, href);
		bread.putIfAbsent(name, crumb);

		log.debug("put Breadcrumbs ...{}", crumb);

		StringBuilder sb = new StringBuilder(headContent.length());
		sb.append(headContent, 0, start).append(headContent.substring(end + 2));
		headContent = sb.toString();

		return headContent.indexOf(breadStartTag, start);
	}

	String getAttribute(String attribute, String breadContent) {
		String attr = attribute + "=\"";
		int start = breadContent.indexOf(attr);
		int end = breadContent.indexOf("\"", start + attr.length() + 1);
		return breadContent.substring(start + attr.length(), end);
	}

	public void mergeChild(PageContent child) {

		log.info("merge child {} parent {}", child.templateName, templateName);
		this.variables.putAll(child.variables);
		this.bread.putAll(child.bread);
		this.metaContent = child.metaContent;
		if (hasParentLayout()) {
			mergeLayout(child);
		} else {
			mergeContentFinal(child);
		}
	}

	private void mergeLayout(PageContent child) {
		mergeLayoutHeadBody(child);
		mergeLayoutBodyTagAttributes(child);
		mergeLayoutTitle(child);
	}

	private void mergeContentFinal(PageContent child) {
		mergeContentHeadBody(child);
		mergeContentBodyTagAttributes(child);
		mergeContentTitle(child.titleTagContent);
	}

	private void mergeLayoutTitle(PageContent child) {
		if (child.titleTagContent != null && !child.titleTagContent.isEmpty()) {
			this.titleTagContent = child.titleTagContent;
		}
	}

	private void mergeLayoutBodyTagAttributes(PageContent child) {
		if (child.bodyTagAttributes != null && !child.bodyTagAttributes.trim().isEmpty()) {
			this.bodyTagAttributes = child.bodyTagAttributes;
		}
	}

	private void mergeLayoutHeadBody(PageContent child) {
		String childHead = child.getHeadContent();
		if (childHead != null) {
			headContent = StringHelper.replaceString(headContent, layoutHeadTag, childHead);
		}
		final String childBody = child.getBodyContent();
		if (childBody != null) {
			bodyContent = StringHelper.replaceString(bodyContent, layoutBodyTag, childBody);
		}
	}

	private void mergeContentHeadBody(PageContent child) {

		String childHead = child.getHeadContent();

		String pg = content;
		int startPos = content.indexOf(layoutHeadTag);
		if (startPos > 0) {
			// insert child head content for <meta id="layout-head"/>
			pg = StringHelper.replaceString(content, layoutHeadTag, childHead, startPos);
		} else {
			int endHeadPos = content.indexOf("</head>");
			if (endHeadPos > -1) {
				// insert just child head content prior to the closing head tag
				StringBuilder temp = new StringBuilder(pg.length() + childHead.length() + 2);
				temp.append(pg.substring(0, endHeadPos));
				temp.append(childHead);
				temp.append(pg.substring(endHeadPos));
				pg = temp.toString();
			}
		}

		// replace body
		content = StringHelper.replaceString(pg, layoutBodyTag, child.getBodyContent());
	}

	private void mergeContentTitle(String titleTagContent) {
		if (titleTagContent != null && titleTagContent.trim().length() > 0) {
			int parentTitleStart = content.indexOf("<title>");
			if (parentTitleStart == -1) {
				throw new RuntimeException("'<title>' tag not found in parent page [" + templateName + "]");
			}
			int parentTitleEnd = content.indexOf("</title>", parentTitleStart);
			if (parentTitleEnd == -1) {
				throw new RuntimeException("'</title>' tag not found in parent page [" + templateName + "]");
			}

			StringBuilder buffer = new StringBuilder(content.length() + 150);
			buffer.append(content, 0, parentTitleStart);
			buffer.append("<title>");
			buffer.append(titleTagContent);
			buffer.append(content.substring(parentTitleEnd));
			content = buffer.toString();
		}
	}

	private void mergeContentBodyTagAttributes(PageContent child) {
		if (child.bodyTagAttributes == null || child.bodyTagAttributes.trim().isEmpty()) {
			return;
		}

		String attr = child.bodyTagAttributes.trim();
		int parentBodyTagStart = content.indexOf("<body");
		if (parentBodyTagStart == -1) {
			throw new RuntimeException("No '<body' found in page " + templateName);
		}
		int parentBodyTagEnd = content.indexOf(_END_TAG, parentBodyTagStart);
		if (parentBodyTagEnd == -1) {
			throw new RuntimeException("No '>' found for '<body' in page " + templateName);
		}
		StringBuilder b = new StringBuilder(content.length() + attr.length() + 10);
		b.append(content, 0, parentBodyTagStart);
		b.append("<body ");
		b.append(attr);
		b.append(_END_TAG);
		b.append(content.substring(parentBodyTagEnd + 1));

		content = b.toString();
	}

	private String getBodyContent() {
		return bodyContent;
	}

	private String getHeadContent() {
		return headContent;
	}

	private static class Crumb {

		String name;
		String desc;
		String href;

		Crumb(String name, String desc, String href) {
			this.name = name;
			this.desc = desc;
			this.href = href;
		}

		String render(boolean isAnchor) {
			if (isAnchor) {
				return "<a href=\"" + href + "\">" + desc + "</a>";
			} else {
				return desc;
			}
		}

		public String toString() {
			return name + " " + desc + " " + href;
		}
	}

}
