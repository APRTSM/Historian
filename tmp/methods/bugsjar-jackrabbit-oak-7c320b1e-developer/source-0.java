    public static void escape(String s, int length, StringBuilder buff) {
        // TODO only backslashes, double quotes, and characters < 32 need to be
        // escaped - but currently all special characters are escaped, which
        // needs more time, memory, and storage space
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
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
                    // guaranteed to be 1 or 2 hex digits only
                    buff.append("\\u00");
                    String hex = Integer.toHexString(c);
                    if (hex.length() == 1) {
                        buff.append('0');
                    }
                    buff.append(hex);
                } else if (c >= 127) {
                    // ascii only mode
                    buff.append("\\u");
                    String hex = Integer.toHexString(c);
                    for (int len = hex.length(); len < 4; len++) {
                        buff.append('0');
                    }
                    buff.append(hex);
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
            if (c == '\"' || c == '\\' || c < ' ' || c >= 127) {
                StringBuilder buff = new StringBuilder(length + 2 + length / 8);
                buff.append('\"');
                escape(s, length, buff);
                return buff.append('\"').toString();
            }
        }
        StringBuilder buff = new StringBuilder(length + 2);
        return buff.append('\"').append(s).append('\"').toString();
    }
