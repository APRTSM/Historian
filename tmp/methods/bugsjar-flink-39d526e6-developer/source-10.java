	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Byte reusable) {
		int val = 0;
		boolean neg = false;

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
	public static final byte parseField(byte[] bytes, int startPos, int length, char delimiter) {
		if (length <= 0) {
			throw new NumberFormatException("Invalid input: Empty string");
		}
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
				return (byte) (neg ? -val : val);
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

		final int delimLimit = limit-delimiter.length+1;
		
		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}
		
		String str = new String(bytes, startPos, i-startPos);
		try {
			this.result = Double.parseDouble(str);
			return (i == limit) ? limit : i + delimiter.length;
		}
		catch (NumberFormatException e) {
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
		
		String str = new String(bytes, startPos, i);
		return Double.parseDouble(str);
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, DoubleValue reusable) {
		
		int i = startPos;

		final int delimLimit = limit-delimiter.length+1;

		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}
		
		String str = new String(bytes, startPos, i-startPos);
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
	public static final float parseField(byte[] bytes, int startPos, int length, char delimiter) {
		if (length <= 0) {
			throw new NumberFormatException("Invalid input: Empty string");
		}
		int i = 0;
		final byte delByte = (byte) delimiter;
		
		while (i < length && bytes[i] != delByte) {
			i++;
		}
		
		String str = new String(bytes, startPos, i);
		return Float.parseFloat(str);
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Float reusable) {
		
		int i = startPos;

		final int delimLimit = limit-delimiter.length+1;

		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}
		
		String str = new String(bytes, startPos, i-startPos);
		try {
			this.result = Float.parseFloat(str);
			return (i == limit) ? limit : i+ delimiter.length;
		}
		catch (NumberFormatException e) {
			setErrorState(ParseErrorState.NUMERIC_VALUE_FORMAT_ERROR);
			return -1;
		}
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, FloatValue reusable) {
		
		int i = startPos;

		final int delimLimit = limit-delimiter.length+1;

		while (i < limit) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				break;
			}
			i++;
		}
		
		String str = new String(bytes, startPos, i-startPos);
		try {
			float value = Float.parseFloat(str);
			reusable.setValue(value);
			this.result = reusable;
			return (i == limit) ? limit : i + delimiter.length;
		}
		catch (NumberFormatException e) {
			setErrorState(ParseErrorState.NUMERIC_VALUE_FORMAT_ERROR);
			return -1;
		}
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Integer reusable) {
		long val = 0;
		boolean neg = false;

		final int delimLimit = limit-delimiter.length+1;

		if (bytes[startPos] == '-') {
			neg = true;
			startPos++;
			
			// check for empty field with only the sign
			if (startPos == limit || ( startPos < delimLimit && delimiterNext(bytes, startPos, delimiter))) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ORPHAN_SIGN);
				return -1;
			}
		}
		
		for (int i = startPos; i < limit; i++) {
			if (i < delimLimit && delimiterNext(bytes, i, delimiter)) {
				this.result = (int) (neg ? -val : val);
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;
			
			if (val > OVERFLOW_BOUND && (!neg || val > UNDERFLOW_BOUND)) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
				return -1;
			}
		}
		
		this.result = (int) (neg ? -val : val);
		return limit;
	}
	public static final int parseField(byte[] bytes, int startPos, int length, char delimiter) {
		if (length <= 0) {
			throw new NumberFormatException("Invalid input: Empty string");
		}
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
				return (int) (neg ? -val : val);
			}
			if (bytes[startPos] < 48 || bytes[startPos] > 57) {
				throw new NumberFormatException("Invalid character.");
			}
			val *= 10;
			val += bytes[startPos] - 48;
			
			if (val > OVERFLOW_BOUND && (!neg || val > UNDERFLOW_BOUND)) {
				throw new NumberFormatException("Value overflow/underflow");
			}
		}
		return (int) (neg ? -val : val);
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, IntValue reusable) {
		long val = 0;
		boolean neg = false;

		final int delimLimit = limit-delimiter.length+1;

		this.result = reusable;

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
				reusable.setValue((int) (neg ? -val : val));
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;
			
			if (val > OVERFLOW_BOUND && (!neg || val > UNDERFLOW_BOUND)) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
				return -1;
			}
		}
		
		reusable.setValue((int) (neg ? -val : val));
		return limit;
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, Long reusable) {
		long val = 0;
		boolean neg = false;

		final int delimLimit = limit - delimiter.length + 1;
		
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
				this.result = neg ? -val : val;
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;
			
			// check for overflow / underflow
			if (val < 0) {
				// this is an overflow/underflow, unless we hit exactly the Long.MIN_VALUE
				if (neg && val == Long.MIN_VALUE) {
					this.result = Long.MIN_VALUE;
					
					if (i+1 >= limit) {
						return limit; 
					} else if (i+1 < delimLimit && delimiterNext(bytes, i+1, delimiter)) {
						return i + 1 + delimiter.length;
					} else {
						setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
						return -1;
					}
				}
				else {
					setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
					return -1;
				}
			}
		}
		
		this.result = neg ? -val : val;
		return limit;
	}
	public static final long parseField(byte[] bytes, int startPos, int length, char delimiter) {
		if (length <= 0) {
			throw new NumberFormatException("Invalid input: Empty string");
		}
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
				return neg ? -val : val;
			}
			if (bytes[startPos] < 48 || bytes[startPos] > 57) {
				throw new NumberFormatException("Invalid character.");
			}
			val *= 10;
			val += bytes[startPos] - 48;
			
			// check for overflow / underflow
			if (val < 0) {
				// this is an overflow/underflow, unless we hit exactly the Long.MIN_VALUE
				if (neg && val == Long.MIN_VALUE) {
					if (length == 1 || bytes[startPos+1] == delimiter) {
						return Long.MIN_VALUE;
					} else {
						throw new NumberFormatException("value overflow");
					}
				}
				else {
					throw new NumberFormatException("value overflow");
				}
			}
		}
		return neg ? -val : val;
	}
	public int parseField(byte[] bytes, int startPos, int limit, byte[] delimiter, LongValue reusable) {
		long val = 0;
		boolean neg = false;

		final int delimLimit = limit-delimiter.length+1;

		this.result = reusable;
		
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
				reusable.setValue(neg ? -val : val);
				return i + delimiter.length;
			}
			if (bytes[i] < 48 || bytes[i] > 57) {
				setErrorState(ParseErrorState.NUMERIC_VALUE_ILLEGAL_CHARACTER);
				return -1;
			}
			val *= 10;
			val += bytes[i] - 48;

			// check for overflow / underflow
			if (val < 0) {
				// this is an overflow/underflow, unless we hit exactly the Long.MIN_VALUE
				if (neg && val == Long.MIN_VALUE) {
					reusable.setValue(Long.MIN_VALUE);
					
					if (i+1 >= limit) {
						return limit;
					} else if (i+1 < delimLimit && delimiterNext(bytes, i+1, delimiter)) {
						return i + 1 + delimiter.length;
					} else {
						setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
						return -1;
					}
				}
				else {
					setErrorState(ParseErrorState.NUMERIC_VALUE_OVERFLOW_UNDERFLOW);
					return -1;
				}
			}
		}

		reusable.setValue(neg ? -val : val);
		return limit;
	}
