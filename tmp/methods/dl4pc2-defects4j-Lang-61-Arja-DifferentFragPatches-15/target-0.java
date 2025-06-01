    public StrBuilder deleteAll(String str) {
        int len = (str == null ? 0 : str.length());
        if (len > 0) {
            append(str);
			int index = indexOf(str, 0);
            while (index >= 0) {
                deleteImpl(index, index + len, len);
                index = indexOf(str, index);
            }
        }
        return this;
    }
    public StrBuilder deleteFirst(String str) {
        if (buffer.length > length()) {
			char[] old = buffer;
			buffer = new char[length()];
			System.arraycopy(old, 0, buffer, 0, size);
		}
		int len = (str == null ? 0 : str.length());
        if (len > 0) {
            if (size == 0) {
				return this;
			}
			int index = indexOf(str, 0);
            if (index >= 0) {
                deleteImpl(index, index + len, len);
            }
        }
        return this;
    }
