    private static void escape(String s, int length, StringBuilder buff) {
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            int ic = (int)c;
            switch (c) {
            case '"':
                // quotation mark
                buff.append("\\\"");
                break;
            case '\\':
                // backslash
                buff.append("\\\\");
                break;
            case '\b':
                // backspace
                buff.append("\\b");
                break;
            case '\f':
                // formfeed
                buff.append("\\f");
                break;
            case '\n':
                // newline
                buff.append("\\n");
                break;
            case '\r':
                // carriage return
                buff.append("\\r");
                break;
            case '\t':
                // horizontal tab
                buff.append("\\t");
                break;
            default:
                if (c < ' ') {
                    buff.append(String.format("\\u%04x", ic));
                } else if (ic >= 0xD800 && ic <= 0xDBFF) {
                    // isSurrogate(), only available in Java 7
                    if (i < length - 1 && Character.isSurrogatePair(c, s.charAt(i + 1))) {
                        // ok surrogate
                        buff.append(c);
                        buff.append(s.charAt(i + 1));
                        i += 1;
                    } else {
                        // broken surrogate -> escape
                        buff.append(String.format("\\u%04x", ic));
                    }
                } else {
                    buff.append(c);
                }
            }
        }
    }
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
