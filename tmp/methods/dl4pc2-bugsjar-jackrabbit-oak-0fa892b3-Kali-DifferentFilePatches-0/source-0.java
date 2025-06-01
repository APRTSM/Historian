    public static String encode(String s) {
        if (s == null) {
            return "null";
        }
        int length = s.length();
        if (length == 0) {
            return "\"\"";
        }
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c == '\"' || c == '\\' || c < ' ') {
                StringBuilder buff = new StringBuilder(length + 2 + length / 8);
                buff.append('\"');
                escape(s, length, buff);
                return buff.append('\"').toString();
            }
        }
        StringBuilder buff = new StringBuilder(length + 2);
        return buff.append('\"').append(s).append('\"').toString();
    }
