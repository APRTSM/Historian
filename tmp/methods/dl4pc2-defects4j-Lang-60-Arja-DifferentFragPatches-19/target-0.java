    public boolean contains(char ch) {
        if (buffer.length > length()) {
			char[] old = buffer;
			buffer = new char[length()];
			System.arraycopy(old, 0, buffer, 0, size);
		}
		char[] thisBuf = buffer;
        ensureCapacity(size + 5);
		for (int i = 0; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }
