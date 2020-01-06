package org.avaje.freemarker.layout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Raw string manipulation of HTML templates handling layout inheritance and dealing with HEAD
 * sections, TITLE etc.
 */
class RawTemplateInherit {

	private static final Logger log = LoggerFactory.getLogger(RawTemplateInherit.class);

	private final RawTemplateSource templateSource;
	private final ContentFilter contentFilter;

	RawTemplateInherit(RawTemplateSource templateSource, ContentFilter contentFilter) {
		this.templateSource = templateSource;
		this.contentFilter = contentFilter;
	}

	public Reader getReader(String templateName, String encoding) throws IOException {
		return new StringReader(getSource(templateName, encoding));
	}

	String getSource(String templateName, String encoding) throws IOException {

		String baseContent = templateSource.getSource(templateName, encoding);
		if (baseContent == null) {
			throw new RuntimeException("template not found: " + templateName);
		}

		PageContent page = new PageContent(templateName, baseContent);

		// detect and merge layout inheritance
		String result = mergeInheritedLayout(encoding, page);
		if (contentFilter != null) {
		  return contentFilter.filter(result);
		} else {
		  return result;
		}
	}

	private String mergeInheritedLayout(String encoding, PageContent page) throws IOException {
		if (!page.hasParentLayout()) {
			return page.renderContent();
		}
		return mergeContent(encoding, page);
	}

	private String mergeContent(String encoding, PageContent page) throws IOException {
		// there is page inheritance
		String parentLayout = page.getParentLayout();
		log.trace("merge parentLayout {}", parentLayout);

		String parentContent = templateSource.getSource(parentLayout, encoding);
		PageContent parentPage = new PageContent(parentLayout, parentContent);
		parentPage.mergeChild(page);

		// recursively inherit
		return mergeInheritedLayout(encoding, parentPage);
	}

}
