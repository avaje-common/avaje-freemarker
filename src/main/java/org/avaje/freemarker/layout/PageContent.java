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

	private String content;
	private String headContent;
	private String titleTagContent;
	private String bodyTagContent;
	private String bodyContent;

	private int headStart;
	private int headEnd;
	private int metaLayoutStart;
	private int metaEnd;

	private int titleStart;
	private int titleEnd;

	private String parentLayout;
	private String headTagContent;
	private String metaContent;

	PageContent(String templateName, String content) {
		this.templateName = templateName;
		this.content = content;
	}

	boolean noHeadLayout() {
		headStart = content.indexOf(_HEAD);
		if (headStart == -1) {
			return true;
		}
		headEnd = content.indexOf(_HEAD_END, headStart + _HEAD.length());
		if (headEnd == -1) {
			throw new RuntimeException("'" + _HEAD_END + "' tag not found after position [" + headStart + "]");
		}

		// just search the head section for <meta name=\"layout\"
		headContent = content.substring(headStart + 6, headEnd);
		metaLayoutStart = headContent.indexOf("<meta name=\"layout\"");
		if (metaLayoutStart == -1) {
			// no page inheritance for this page
			log.info("no page inheritance ... {}", templateName);

			return true;
		}

		return false;
	}

	String content() {
		log.info("content render ...");

		if (metaContent != null) {
			log.info("content render ... metaContent");
			content = StringHelper.replaceString(content, metaLayoutBodyTag, metaContent);
		}
		if (!variables.isEmpty()) {
			log.info("content render ... variables {}", variables);
			for (Map.Entry<String, String> entry : variables.entrySet()) {
				String varId = entry.getKey();
				String varContent = entry.getValue();
				content = StringHelper.replaceString(content, "$" + varId, varContent);
			}
		}

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

			String crumConte = crumbs.toString();
			log.info("content render ... crumConte {}", crumConte);
			content = StringHelper.replaceString(content, "$breadcrumb", crumConte);
		}

		return content;
	}

	public void metaLayoutEnd() {

		log.info("read metaLayoutEnd ... {}", templateName);

		metaEnd = headContent.indexOf(_END_TAG, metaLayoutStart + 13);
		if (metaEnd == -1) {
			throw new RuntimeException("'>' not found for <meta name=\"content\" at pos[" + metaLayoutStart + 13 + "]");
		}
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

		findMetaContent();
	}

	private void findMetaContent() {
		int metaContentStart = headContent.indexOf("<template name=\"meta-content\">");
		if (metaContentStart > -1) {
			int metaContentEnd = headContent.indexOf("</template>", metaContentStart + 32);
			if (metaContentEnd == -1) {
				throw new RuntimeException("'</template>' not found for <template name=\"meta-content\" at pos[" + metaContentStart + 32 + "]");
			}
			metaContent = headContent.substring(metaContentStart + 32, metaContentEnd).trim();

			StringBuilder sb = new StringBuilder(headContent.length());
			sb.append(headContent.substring(0, metaContentStart));
			sb.append(headContent.substring(metaContentEnd + 11));
			headContent = sb.toString();
		}
		findVariables();
		findBreadcrumbs();
	}

	private void findVariables() {
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


		log.info("put variable ... {} {}", varId, varContent);

		StringBuilder sb = new StringBuilder(headContent.length());
		sb.append(headContent.substring(0, start));
		sb.append(headContent.substring(end + 11));
		headContent = sb.toString();

		return headContent.indexOf("<template id=\"", start);
	}

	private void findBreadcrumbs() {
		log.info("findBreadcrumbs ...");
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

		log.info("put Breadcrumbs ...{}", crumb);

		StringBuilder sb = new StringBuilder(headContent.length());
		sb.append(headContent.substring(0, start));
		sb.append(headContent.substring(end + 2));
		headContent = sb.toString();

		return headContent.indexOf(breadStartTag, start);
	}

	String getAttribute(String attribute, String breadContent) {
		String attr = attribute + "=\"";
		int start = breadContent.indexOf(attr);
		int end = breadContent.indexOf("\"", start + attr.length() + 1);
		return breadContent.substring(start + attr.length(), end);
	}

	public void readBody() {

		int bodyTagStart = content.indexOf("<body", headEnd + 7);
		if (bodyTagStart == -1) {
			throw new RuntimeException("'<body' tag not found after position [" + headEnd + 7 + "]");
		}
		int bodyTagEnd = content.indexOf(_END_TAG, bodyTagStart + 5);
		if (bodyTagEnd == -1) {
			throw new RuntimeException("'>' character not found after '<body' position [" + bodyTagStart + 5 + "]");
		}
		int bodyEnd = content.lastIndexOf("</body>", content.length());
		if (bodyEnd == -1) {
			throw new RuntimeException("'</body>' tag not found after position [" + bodyTagEnd + "]");
		}

		// get the body tag content
		bodyTagContent = content.substring(bodyTagStart + 5, bodyTagEnd);
		bodyContent = content.substring(bodyTagEnd + 1, bodyEnd);
	}

	public String readHeadMeta() {
		// get head content (minus title, minus meta layout)
		headTagContent = getHeadTagContent(headContent);
		parentLayout = getMetaTagContent(headContent);
		return parentLayout;
	}


	private String getMetaTagContent(String headContent) {//}, int metaStart, int metaEnd) {

		int tnStart = headContent.indexOf("content=", metaLayoutStart);
		if (tnStart == -1) {
			throw new RuntimeException("No 'content=' attribute in <meta name=\"layout\" ... element?");
		}
		String t = headContent.substring(tnStart + 8, metaEnd);
		if (t.endsWith("/")) {
			t = t.substring(0, t.length() - 1);
		}

		char[] removeChars = {'"', '\''};
		t = StringHelper.removeChars(t, removeChars);
		return t.trim();
	}

	private String getHeadTagContent(String headContent) {//}, int metaStart, int metaEnd, int titleEnd, int titleStart) {

		StringBuilder sb = new StringBuilder(headContent.length());
		if (titleStart < metaLayoutStart) {
			if (titleStart > -1) {
				sb.append(headContent.substring(0, titleStart));
				sb.append(headContent.substring(titleEnd + 8, metaLayoutStart));
				sb.append(headContent.substring(metaEnd + 1, headContent.length()));
			} else {
				sb.append(headContent.substring(0, metaLayoutStart));
				sb.append(headContent.substring(metaEnd + 1, headContent.length()));
			}
		} else {
			sb.append(headContent.substring(0, metaLayoutStart));
			sb.append(headContent.substring(metaEnd + 1, titleStart));
			sb.append(headContent.substring(titleEnd + 8, headContent.length()));
		}

		return sb.toString().trim();
	}

	public void mergeChild(PageContent child) {

		log.info("merge child {} parent {}", child.templateName, templateName);

		this.variables = child.variables;
		this.bread = child.bread;
		this.metaContent = child.metaContent;
		mergeHeadAndBody(child);
		mergeBodyTagAttributes(child);
		mergeTitleTag(child.titleTagContent);
	}

	private void mergeTitleTag(String titleTagContent) {

		if (titleTagContent != null && titleTagContent.trim().length() > 0) {
			int parentTitleStart = content.indexOf("<title>");
			if (parentTitleStart == -1) {
				throw new RuntimeException("'<title>' tag not found int parent page [" + templateName + "]");
			}
			int parentTitleEnd = content.indexOf("</title>", parentTitleStart);
			if (parentTitleEnd == -1) {
				throw new RuntimeException("'</title>' tag not found int parent page [" + templateName + "]");
			}

			StringBuilder b = new StringBuilder(content.length() + 150);
			b.append(content.substring(0, parentTitleStart));
			b.append("<title>");
			b.append(titleTagContent);
			b.append(content.substring(parentTitleEnd, content.length()));

			content = b.toString();
		}
	}

	private void mergeBodyTagAttributes(PageContent child) {

		if (child.bodyTagContent == null || child.bodyTagContent.trim().isEmpty()) {
			return;
		}

		String attr = child.bodyTagContent.trim();
		int parentBodyTagStart = content.indexOf("<body");
		if (parentBodyTagStart == -1) {
			throw new RuntimeException("No '<body' found in page " + templateName);
		}
		int parentBodyTagEnd = content.indexOf(_END_TAG, parentBodyTagStart);
		if (parentBodyTagEnd == -1) {
			throw new RuntimeException("No '>' found for '<body' in page " + templateName);
		}
		StringBuilder b = new StringBuilder(content.length() + attr.length() + 10);
		b.append(content.substring(0, parentBodyTagStart));
		b.append("<body ");
		b.append(attr);
		b.append(_END_TAG);
		b.append(content.substring(parentBodyTagEnd + 1, content.length()));

		content = b.toString();
	}

	private void mergeHeadAndBody(PageContent child) {

		String childHead = child.getHeadTagContent();

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
		pg = StringHelper.replaceString(pg, layoutBodyTag, child.getBodyContent());

		this.content = pg;
	}

	private String getBodyContent() {
		return bodyContent;
	}

	private String getHeadTagContent() {

		return headTagContent;
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
