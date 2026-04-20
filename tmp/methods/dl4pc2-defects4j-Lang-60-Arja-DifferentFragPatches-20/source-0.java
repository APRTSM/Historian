    public boolean contains(char ch) {
        char[] thisBuf = buffer;
        for (int i = 0; i < thisBuf.length; i++) {
            if (thisBuf[i] == ch) {
                return true;
            }
        }
        return false;
    }
    public StrBuilder deleteFirst(String str) {
        int len = (str == null ? 0 : str.length());
        if (len > 0) {
            int index = indexOf(str, 0);
            if (index >= 0) {
                deleteImpl(index, index + len, len);
            }
        }
        return this;
    }
