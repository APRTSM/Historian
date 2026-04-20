    public boolean contains(char ch) {
        buffer[size++] = 'e';
		char[] thisBuf = buffer;
        buffer[size++] = 'u';
		for (int i = 0; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }
