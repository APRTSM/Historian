	protected int skipFields(byte[] bytes, int startPos, int limit, byte[] delim) {

		int i = startPos;

		final int delimLimit = limit - delim.length + 1;

		if(quotedStringParsing == true && bytes[i] == quoteCharacter) {

			// quoted string parsing enabled and field is quoted
			// search for ending quote character
			i++;
			while(i < limit && bytes[i] != quoteCharacter) {
				i++;
			}
			i++;

			if (i == limit) {
				// we are at the end of the record
				return limit;
			} else if ( i < delimLimit && FieldParser.delimiterNext(bytes, i, delim)) {
				// we are not at the end, check if delimiter comes next
				return i + delim.length;
			} else {
				// delimiter did not follow end quote. Error...
				return -1;
			}
		} else {
			// field is not quoted
			while(i < delimLimit && !FieldParser.delimiterNext(bytes, i, delim)) {
				i++;
			}

			if (i >= delimLimit) {
				// no delimiter found. We are at the end of the record
				return limit;
			} else {
				// delimiter found.
				return i + delim.length;
			}
		}
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, String reusable) {

		int i = startPos;

		final int delimLimit = limit-delimiter.length+1;

		if(quotedStringParsing && bytes[i] == quoteCharacter) {
			// quoted string parsing enabled and first character Vis a quote
			i++;

			// search for ending quote character
			while(i < limit && bytes[i] != quoteCharacter) {
				i++;
			}

			if (i == limit) {
				setErrorState(ParseErrorState.UNTERMINATED_QUOTED_STRING);
				return -1;
			} else {
				i++;
				// check for proper termination
				if (i == limit) {
					// either by end of line
					this.result = new String(bytes, startPos+1, i - startPos - 2);
					return limit;
				} else if ( i < delimLimit && delimiterNext(bytes, i, delimiter)) {
					// or following field delimiter
					this.result = new String(bytes, startPos+1, i - startPos - 2);
					return i + delimiter.length;
				} else {
					// no proper termination
					setErrorState(ParseErrorState.UNQUOTED_CHARS_AFTER_QUOTED_STRING);
					return -1;
				}
			}
		} else {

			// look for delimiter
			while( i < delimLimit && !delimiterNext(bytes, i, delimiter)) {
				i++;
			}

			if (i >= delimLimit) {
				// no delimiter found. Take the full string
				this.result = new String(bytes, startPos, limit - startPos);
				return limit;
			} else {
				// delimiter found.
				this.result = new String(bytes, startPos, i - startPos);
				return i + delimiter.length;
			}
		}
	}
