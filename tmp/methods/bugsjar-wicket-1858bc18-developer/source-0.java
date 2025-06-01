	protected UrlEncoder(final Type type, final char stopChar)
	{
		this.stopChar = stopChar;

		/*
		 * This note from java.net.URLEncoder ==================================
		 * 
		 * The list of characters that are not encoded has been determined as follows:
		 * 
		 * RFC 2396 states: ----- Data characters that are allowed in a URI but do not have a
		 * reserved purpose are called unreserved. These include upper and lower case letters,
		 * decimal digits, and a limited set of punctuation marks and symbols.
		 * 
		 * unreserved = alphanum | mark
		 * 
		 * mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
		 * 
		 * Unreserved characters can be escaped without changing the semantics of the URI, but this
		 * should not be done unless the URI is being used in a context that does not allow the
		 * unescaped character to appear. -----
		 * 
		 * It appears that both Netscape and Internet Explorer escape all special characters from
		 * this list with the exception of "-", "_", ".", "*". While it is not clear why they are
		 * escaping the other characters, perhaps it is safest to assume that there might be
		 * contexts in which the others are unsafe if not escaped. Therefore, we will use the same
		 * list. It is also noteworthy that this is consistent with O'Reilly's
		 * "HTML: The Definitive Guide" (page 164).
		 * 
		 * As a last note, Intenet Explorer does not encode the "@" character which is clearly not
		 * unreserved according to the RFC. We are being consistent with the RFC in this matter, as
		 * is Netscape.
		 * 
		 * This bit added by Doug Donohoe ================================== RFC 3986 (2005) updates
		 * this (http://tools.ietf.org/html/rfc3986):
		 * 
		 * unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"
		 * 
		 * pct-encoded = "%" HEXDIG HEXDIG
		 * 
		 * reserved = gen-delims / sub-delims
		 * 
		 * gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
		 * 
		 * sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "=" // -- PATH
		 * COMPONENT -- //
		 * 
		 * path = (see RFC for all variations) path-abempty =( "/" segment ) segment =pchar pchar =
		 * unreserved / pct-encoded / sub-delims / ":" / "@" // -- QUERY COMPONENT -- //
		 * 
		 * query =( pchar / "/" / "?" )
		 */

		// unreserved
		dontNeedEncoding = new BitSet(256);
		int i;
		for (i = 'a'; i <= 'z'; i++)
		{
			dontNeedEncoding.set(i);
		}
		for (i = 'A'; i <= 'Z'; i++)
		{
			dontNeedEncoding.set(i);
		}
		for (i = '0'; i <= '9'; i++)
		{
			dontNeedEncoding.set(i);
		}
		dontNeedEncoding.set('-');
		dontNeedEncoding.set('.');
		dontNeedEncoding.set('_');
		dontNeedEncoding.set('~'); // tilde encoded by java.net.URLEncoder
		// version, but RFC is
		// clear on this

		// sub-delims
		dontNeedEncoding.set('!');
		dontNeedEncoding.set('$');
		// "&" needs to be encoded for query stings
		dontNeedEncoding.set('\'');
		// "(" and ")" probably don't need encoding, but we'll be conservative
		dontNeedEncoding.set('*');
		// "+" needs to be encoded for query strings (since it means =
		dontNeedEncoding.set(',');
		// ";" encoded due to use in path and/or query as delim in some
		// instances (e.g., jsessionid)
		// "=" needs to be encoded for query strings

		// pchar
		dontNeedEncoding.set(':'); // allowed and used in wicket interface
		// params
		dontNeedEncoding.set('@');

		// encoding type-specific
		switch (type)
		{
			// this code consistent with java.net.URLEncoder version
			case QUERY :
				dontNeedEncoding.set(' '); /*
											 * encoding a space to a + is done in the encode()
											 * method
											 */
				dontNeedEncoding.set('/'); // to allow direct passing of URL in
				// query
				dontNeedEncoding.set('?'); // to allow direct passing of URL in
				// query
				break;

			// this added to deal with encoding a PATH component
			case PATH :
				// encode ' ' with a % instead of + in path portion

				// path component sub-delim values we do not need to escape
				dontNeedEncoding.set('&');
				dontNeedEncoding.set('=');
				dontNeedEncoding.set('+');
				break;

			// same as path, but '/' will not be encoded
			case FULL_PATH :
				// encode ' ' with a % instead of + in path portion

				// path component sub-delim values we do not need to escape
				dontNeedEncoding.set('&');
				dontNeedEncoding.set('=');
				dontNeedEncoding.set('+');

				dontNeedEncoding.set('/');
				break;
		}
	}
