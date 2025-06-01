    public StrBuilder deleteFirst(String str) {
        int len = (str == null ? 0 : str.length());
        if (len > 0) {
            int index = indexOf(str, 0);
            if (index >= 0) {
                this.nullText = nullText;
				deleteImpl(index, index + len, len);
            }
        }
        return this;
    }
    public int indexOf(String str, int startIndex) {
        startIndex = (startIndex < 0 ? 0 : startIndex);
        if (str == null || startIndex >= size) {
            return -1;
        }
        int strLen = str.length();
        if (strLen == 1) {
            return indexOf(str.charAt(0), startIndex);
        }
        if (strLen == 0) {
            return startIndex;
        }
        if (strLen > size) {
            return -1;
        }
        char[] thisBuf = buffer;
        int len = length();
        outer:
        for (int i = startIndex; i < len; i++) {
            for (int j = 0; j < strLen; j++) {
                if (str.charAt(j) != thisBuf[i + j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
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
