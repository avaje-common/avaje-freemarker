package org.avaje.freemarker.layout;


/**
 * Utility String class that supports String manipulation functions.
 */
class StringHelper {


	/**
	 * This method takes a String and will replace all occurrences of the match
	 * String with that of the replace String.
	 * 
	 * @param source
	 *            the source string
	 * @param match
	 *            the string used to find a match
	 * @param replace
	 *            the string used to replace match with
	 * @return the source string after the search and replace
	 */
	public static String replaceString(String source, String match, String replace) {
		return replaceString(source, match, replace, 0);
	}
	
	/**
	 * Search and replace starting the search part way through the string.
	 */
  public static String replaceString(String source, String match, String replace, int startSearchPos) {
    if (source == null) {
      return null;
    }
    if (replace == null) {
      return source;
    }
    if (match == null) {
      throw new NullPointerException("match is null?");
    }
    if (match.equals(replace)) {
      return source;
    }
    int extra = replace.length() - match.length();
    if (extra < 0) {
      extra = 0;
    }
    return replaceString(source, match, replace, extra, startSearchPos, source.length());
  }

	/**
	 * Additionally specify the additionalSize to add to the buffer. This will
	 * make the buffer bigger so that it doesn't have to grow when replacement
	 * occurs.
	 */
	public static String replaceString(String source, String match, String replace,
			int additionalSize, int startPos, int endPos) {

		if (source == null){
			return source;
		}
		
		char match0 = match.charAt(0);
		
		int matchLength = match.length();

		if (matchLength == 1 && replace.length() == 1) {
			char replace0 = replace.charAt(0);
			return source.replace(match0, replace0);
		}
		if (matchLength >= replace.length()) {
			additionalSize = 0;
		}


		int sourceLength = source.length();
		int lastMatch = endPos - matchLength;

		StringBuilder sb = new StringBuilder(sourceLength + additionalSize);

		if (startPos > 0) {
			sb.append(source.substring(0, startPos));
		}

		char sourceChar;
		boolean isMatch;
		int sourceMatchPos;

		for (int i = startPos; i < sourceLength; i++) {
			sourceChar = source.charAt(i);
			if (i > lastMatch || sourceChar != match0) {
				sb.append(sourceChar);

			} else {
				// check to see if this is a match
				isMatch = true;
				sourceMatchPos = i;
				
				// check each following character...
				for (int j = 1; j < matchLength; j++) {
					sourceMatchPos++;
					if (source.charAt(sourceMatchPos) != match.charAt(j)) {
						isMatch = false;
						break;
					}
				}
				if (isMatch) {
					i = i + matchLength - 1;
					sb.append(replace);
				} else {
					// was not a match
					sb.append(sourceChar);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * A search and replace with multiple matching strings.
	 * <p>
	 * Useful when converting CRNL CR and NL all to a BR tag for example.
	 * </p>
	 * 
	 * <pre><code>
	 * String[] multi = { &quot;\r\n&quot;, &quot;\r&quot;, &quot;\n&quot; };
	 * content = StringHelper.replaceStringMulti(content, multi, &quot;&lt;br/&gt;&quot;);
	 * </code></pre>
	 */
	static String replaceStringMulti(String source, String[] match, String replace) {
		return replaceStringMulti(source, match, replace, 30, 0, source.length());
	}

	/**
	 * Additionally specify an additional size estimate for the buffer plus
	 * start and end positions.
	 * <p>
	 * The start and end positions can limit the search and replace. Otherwise
	 * these default to startPos = 0 and endPos = source.length().
	 * </p>
	 */
	static String replaceStringMulti(String source, String[] match, String replace,
			int additionalSize, int startPos, int endPos) {

		int shortestMatch = match[0].length();

		char[] match0 = new char[match.length];
		for (int i = 0; i < match0.length; i++) {
			match0[i] = match[i].charAt(0);
			if (match[i].length() < shortestMatch) {
				shortestMatch = match[i].length();
			}
		}

		StringBuilder sb = new StringBuilder(source.length() + additionalSize);

		char sourceChar;

		int len = source.length();
		int lastMatch = endPos - shortestMatch;

		if (startPos > 0) {
			sb.append(source.substring(0, startPos));
		}

		int matchCount = 0;

		for (int i = startPos; i < len; i++) {
			sourceChar = source.charAt(i);
			if (i > lastMatch) {
				sb.append(sourceChar);
			} else {
				matchCount = 0;
				for (int k = 0; k < match0.length; k++) {
					if (matchCount == 0 && sourceChar == match0[k]) {
						if (match[k].length() + i <= len) {

							++matchCount;
							int j = 1;
							for (; j < match[k].length(); j++) {
								if (source.charAt(i + j) != match[k].charAt(j)) {
									--matchCount;
									break;
								}
							}
							if (matchCount > 0) {
								i = i + j - 1;
								sb.append(replace);
								break;
							}
						}
					}
				}
				if (matchCount == 0) {
					sb.append(sourceChar);
				}
			}
		}

		return sb.toString();
	}


  /**
   * This method takes a String as an argument and removes all occurrences of
   * the supplied Chars. It returns the resulting String.
   */
  static String removeChars(String s, char[] chr) {

    StringBuilder sb = new StringBuilder(s.length());

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (!charMatch(c, chr)) {
        sb.append(c);
      }
    }

    return sb.toString();
  }
	
  private static boolean charMatch(int iChr, char[] chr) {
    for (int i = 0; i < chr.length; i++) {
      if (iChr == chr[i]) {
        return true;
      }
    }
    return false;
  }

}
