package org.avaje.freemarker.layout;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Raw string manipulation of HTML templates handling layout inheritance and dealing with HEAD
 * sections, TITLE etc.
 */
public class RawTemplateInherit {

	private static final String _END_TAG = ">";
	private static final String _HEAD_END = "</head>";
	private static final String _HEAD = "<head>";
	private final RawTemplateSource templateSource;
	private final ContentFilter contentFilter;
	
	private String layoutHeadTag = "<meta id=\"layout-head\"/>";
	private String layoutBodyTag = "<div id=\"layout-body\"></div>";

	public RawTemplateInherit(RawTemplateSource templateSource, ContentFilter contentFilter) {
		this.templateSource = templateSource;
		this.contentFilter = contentFilter;
	}

	public Reader getReader(String templateName, String encoding) throws IOException {
		return new StringReader(getSource(templateName, encoding));
	}

	public String getSource(String templateName, String encoding) throws IOException {

		String pageContent = templateSource.getSource(templateName, encoding);
		if (pageContent == null) {
			throw new RuntimeException("template not found: " + templateName);
		}

		// detect and merge layout inheritance
		String result = mergeInheritedLayout(encoding, templateName, pageContent);
		if (contentFilter != null) {
		  return contentFilter.filter(result);
		} else {
		  return result;
		}
	}

	private String mergeInheritedLayout(String encoding, String templateName, String pageContent) throws IOException {

		int headStart = pageContent.indexOf(_HEAD);
		if (headStart == -1) {
			return pageContent;
		}
		int headEnd = pageContent.indexOf(_HEAD_END, headStart + _HEAD.length());
		if (headEnd == -1) {
			throw new RuntimeException("'" + _HEAD_END + "' tag not found after position [" + headStart + "]");
		}

		// just search the head section for <meta name=\"layout\"
		String headContent = pageContent.substring(headStart + 6, headEnd);

		int metaStart = headContent.indexOf("<meta name=\"layout\"");
		if (metaStart == -1) {
			// no page inheritance for this page
			return pageContent;
		}

		return mergeContent(encoding, pageContent, headEnd, headContent, metaStart);
	}

	private String mergeContent(String encoding, String pageContent, int headEnd, String headContent, int metaStart) throws IOException {
	  
		// there is page inheritance
		int metaEnd = headContent.indexOf(_END_TAG, metaStart + 13);
		if (metaEnd == -1) {
			throw new RuntimeException("'>' not found for <meta name=\"content\" at pos["+ metaStart + 13 + "]");
		}
		int titleEnd = -1;
		int titleStart = headContent.indexOf("<title>");
		if (titleStart > -1) {
			titleEnd = headContent.indexOf("</title>", titleStart);
			if (titleEnd == -1) {
				throw new RuntimeException("'</title>' tag not found after position [" + titleStart + "]");
			}
		}
		int bodyTagStart = pageContent.indexOf("<body", headEnd + 7);
		if (bodyTagStart == -1) {
			throw new RuntimeException("'<body' tag not found after position [" + headEnd + 7 + "]");
		}
		int bodyTagEnd = pageContent.indexOf(_END_TAG, bodyTagStart + 5);
		if (bodyTagEnd == -1) {
			throw new RuntimeException("'>' character not found after '<body' position [" + bodyTagStart + 5 + "]");
		}
		int bodyEnd = pageContent.lastIndexOf("</body>", pageContent.length());
		if (bodyEnd == -1) {
			throw new RuntimeException("'</body>' tag not found after position [" + bodyTagEnd + "]");
		}

		// get title content
		String titleTagContent = null;
		if (titleStart > -1) {
			titleTagContent = headContent.substring(titleStart + 7, titleEnd);
		}

		// get the body tag content
		String bodyTagContent = pageContent.substring(bodyTagStart + 5, bodyTagEnd);
		String bodyContent = pageContent.substring(bodyTagEnd + 1, bodyEnd);

		// get head content (minus title, minus meta layout)
		String headTagContent = getHeadTagContent(headContent, metaStart, metaEnd, titleEnd, titleStart);

		String parentTemplateName = getMetaTagContent(headContent, metaStart, metaEnd);

		String parentContent = templateSource.getSource(parentTemplateName, encoding);

		String pg = mergeHeadAndBody(bodyContent.trim(), headTagContent, parentContent);
		pg = mergeBodyTag(bodyTagContent.trim(), parentTemplateName, pg);
		if (titleTagContent != null) {
		  pg = mergeTitleTag(titleTagContent.trim(), parentTemplateName, pg);
		}

		// recursively inherit
		return mergeInheritedLayout(encoding, null, pg);
	}

	private String mergeTitleTag(String titleTagContent, String parentTemplateName, String pg) {
	  
		if (titleTagContent != null && titleTagContent.trim().length() > 0) {
			int parentTitleStart = pg.indexOf("<title>");
			if (parentTitleStart == -1) {
				throw new RuntimeException("'<title>' tag not found int parent page [" + parentTemplateName + "]");
			}
			int parentTitleEnd = pg.indexOf("</title>", parentTitleStart);
			if (parentTitleEnd == -1) {
				throw new RuntimeException("'</title>' tag not found int parent page [" + parentTemplateName + "]");
			}

			StringBuilder b = new StringBuilder(pg.length() + 150);
			b.append(pg.substring(0, parentTitleStart));
			b.append("<title>");
			b.append(titleTagContent);
			b.append(pg.substring(parentTitleEnd, pg.length()));

			pg = b.toString();
		}
		return pg;
	}

	private String mergeBodyTag(String bodyTagContent, String parentTemplateName, String pg) {
	  
		if (bodyTagContent.trim().length() > 0) {
			int parentBodyTagStart = pg.indexOf("<body");
			if (parentBodyTagStart == -1) {
				throw new RuntimeException("No '<body' found in page " + parentTemplateName);
			}
			int parentBodyTagEnd = pg.indexOf(_END_TAG, parentBodyTagStart);
			if (parentBodyTagEnd == -1) {
				throw new RuntimeException("No '>' found for '<body' in page " + parentTemplateName);
			}
			StringBuilder b = new StringBuilder(pg.length() + bodyTagContent.length());
			b.append(pg.substring(0, parentBodyTagStart));
			b.append("<body ");
			b.append(bodyTagContent);
			b.append(_END_TAG);
			b.append(pg.substring(parentBodyTagEnd + 1, pg.length()));
			pg = b.toString();
		}
		return pg;
	}

	private String mergeHeadAndBody(String bodyContent, String headTagContent, String parentContent) {
	  
	  String pg = parentContent;
	  int startPos = parentContent.indexOf(layoutHeadTag);
	  if (startPos > 0) {
	    pg = StringHelper.replaceString(parentContent, layoutHeadTag, headTagContent, startPos);
	  } else {
	    int endHeadPos = parentContent.indexOf("</head>");
	    if (endHeadPos > -1) {
	      // insert just prior to the closing head tag
	      StringBuilder temp = new StringBuilder(pg.length()+headTagContent.length()+2);
	      temp.append(pg.substring(0, endHeadPos));
	      temp.append(headTagContent);
	      temp.append(pg.substring(endHeadPos)); 
	      pg = temp.toString();
	    }
	  }
		
		pg = StringHelper.replaceString(pg, layoutBodyTag, bodyContent);
		return pg;
	}

  private String getMetaTagContent(String headContent, int metaStart, int metaEnd) {

    int tnStart = headContent.indexOf("content=", metaStart);
    if (tnStart == -1) {
      throw new RuntimeException("No 'content=' attribute in <meta name=\"layout\" ... element?");
    }
    String t = headContent.substring(tnStart + 8, metaEnd);
    if (t.endsWith("/")) {
      t = t.substring(0, t.length()-1);
    }
   
    char[] removeChars = { '"', '\'' };
    t = StringHelper.removeChars(t, removeChars);
    return t.trim();
  }

	private String getHeadTagContent(String headContent, int metaStart, int metaEnd, int titleEnd, int titleStart) {
		
		StringBuilder sb = new StringBuilder(headContent.length());
		if (titleStart < metaStart) {
			if (titleStart > -1) {
				sb.append(headContent.substring(0, titleStart));
				sb.append(headContent.substring(titleEnd + 8, metaStart));
				sb.append(headContent.substring(metaEnd + 1, headContent.length()));
			} else {
				sb.append(headContent.substring(0, metaStart));
				sb.append(headContent.substring(metaEnd + 1, headContent.length()));
			}
		} else {
			sb.append(headContent.substring(0, metaStart));
			sb.append(headContent.substring(metaEnd + 1, titleStart));
			sb.append(headContent.substring(titleEnd + 8, headContent.length()));
		}

		return sb.toString().trim();
	}

}
