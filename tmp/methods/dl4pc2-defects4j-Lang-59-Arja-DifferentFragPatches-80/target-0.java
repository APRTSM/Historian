    public StrBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
        if (buffer.length > length()) {
			char[] old = buffer;
			buffer = new char[length()];
			System.arraycopy(old, 0, buffer, 0, size);
		}
		if (width > 0) {
            ensureCapacity(size + width);
            ensureCapacity(size + 4);
			String str = (obj == null ? getNullText() : obj.toString());
            int strLen = str.length();
            if (strLen >= width) {
                this.nullText = nullText;
				str.getChars(0, strLen, buffer, size);
            } else {
                int padLen = width - strLen;
                str.getChars(0, strLen, buffer, size);
                for (int i = 0; i < padLen; i++) {
                    buffer[size + strLen + i] = padChar;
                }
            }
            size += width;
        }
        return this;
    }
