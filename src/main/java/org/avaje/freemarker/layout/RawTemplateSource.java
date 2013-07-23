package org.avaje.freemarker.layout;

import java.io.IOException;

public interface RawTemplateSource {

	public String getSource(String templateName, String encoding) throws IOException;
	
}
