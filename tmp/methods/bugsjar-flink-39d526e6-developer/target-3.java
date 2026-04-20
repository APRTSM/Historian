	public static final byte parseField(byte[] bytes, int startPos, int length, char delimiter) {
		long val = 0;
		boolean neg = false;

		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;
			length--;
			if (length == 0 || bytes[startPos] == delimiter) {
				throw new NumberFormatException("Orphaned minus sign.");
			}
		}

		for (; length > 0; startPos++, length--) {
			if (bytes[startPos] == delimiter) {
				throw new NumberFormatException("Empty field.");
			}
			if (bytes[startPos] < 48 || bytes[startPos] > 57) {
				throw new NumberFormatException("Invalid character.");
			}
			val *= 10;
			val += bytes[startPos] - 48;

			if (val > Byte.MAX_VALUE && (!neg || val > -Byte.MIN_VALUE)) {
				throw new NumberFormatException("Value overflow/underflow");
			}
		}
		return (byte) (neg ? -val : val);
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Byte reusable) {
		int val = 0;
		boolean neg = false;

		final int delimLimit = limit - delimiter.length + 1;

		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;

			// check for empty field with only the sign
			if (startPos == limit || (startPos < delimLimit && delimiterNext(bytes, startPos,
				delimiter))) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ORPHAN_SIGN);
				return -1;
			}
		}

		for (int i = startPos; i < limit; i++) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				if (i == startPos) {
					setErrorState(ParseErrorState.EMPTY_STRING);
					return -1;
				}
				this.result = (byte) (neg ? -val : val);
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;

			if (val > Byte.MAX_VALUE && (!neg || val > -Byte.MIN_VALUE)) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
				return -1;
			}
		}

		this.result = (byte) (neg ? -val : val);
		return limit;
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, ByteValue reusable) {
		int val = 0;
		boolean neg = false;
		
		this.result = reusable;

		final int delimLimit = limit-delimiter.length+1;
		
		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;
			
			// check for empty field with only the sign
			if (startPos == limit || (startPos < delimLimit && delimiterNext(bytes, startPos, delimiter))) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ORPHAN_SIGN);
				return -1;
			}
		}

		for (int i = startPos; i < limit; i++) {

			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				if (i == startPos) {
					setErrorState(ParseErrorState.EMPTY_STRING);
					return -1;
				}
				reusable.setValue((byte) (neg ? -val : val));
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;
			
			if (val > Byte.MAX_VALUE && (!neg || val > -Byte.MIN_VALUE)) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
				return -1;
			}
		}
		
		reusable.setValue((byte) (neg ? -val : val));
		return limit;
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Double reusable) {
		int i = startPos;

		final int delimLimit = limit - delimiter.length + 1;

		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}

		String str = new String(bytes, startPos, i - startPos);
		int len = str.length();
		if (Character.isWhitespace(bytes[startPos]) || Character.isWhitespace(bytes[Math.max(i - 1, 0)])) {
			setErrorState(ParseErrorState.WHITESPACE_IN_NUMERIC_FIELD);
			return -1;
		}
		try {
			this.result = Double.parseDouble(str);
			return (i == limit) ? limit : i + delimiter.length;
		} catch (NumberFormatException e) {
			setErrorState(ParseErrorState.NUMERIC_VALUE_FORMAT_ERROR);
			return -1;
		}
	}
	public static final double parseField(byte[] bytes, int startPos, int length, char delimiter) {
		if (length <= 0) {
			throw new NumberFormatException("Invalid input: Empty string");
		}
		int i = 0;
		final byte delByte = (byte) delimiter;

		while (i < length && bytes[i] != delByte) {
			i++;
		}

		String str = new String(bytes, startPos, i - startPos);
		int len = str.length();
		if (Character.isWhitespace(bytes[startPos]) || Character.isWhitespace(bytes[Math.max(i - 1, 0)])) {
			throw new NumberFormatException("There is leading or trailing whitespace in the " +
				"numeric field: " + str);
		}
		return Double.parseDouble(str);
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, DoubleValue reusable) {
		
		int i = startPos;

		final int delimLimit = limit - delimiter.length + 1;

		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}
		
		String str = new String(bytes, startPos, i - startPos);
		if (Character.isWhitespace(bytes[startPos]) || Character.isWhitespace(bytes[Math.max(i - 1, 0)])) {
			setErrorState(ParseErrorState.WHITESPACE_IN_NUMERIC_FIELD);
			return -1;
		}
		try {
			double value = Double.parseDouble(str);
			reusable.setValue(value);
			this.result = reusable;
			return (i == limit) ? limit : i + delimiter.length;
		}
		catch (NumberFormatException e) {
			setErrorState(ParseErrorState.NUMERIC_VALUE_FORMAT_ERROR);
			return -1;
		}
	}
