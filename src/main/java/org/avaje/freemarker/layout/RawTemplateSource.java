package org.avaje.freemarker.layout;

import java.io.IOException;

public interface RawTemplateSource {

	String getSource(String templateName, String encoding) throws IOException;
	
}
