    private static void escape(String s, int length, StringBuilder buff) {
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
                    buff.append("\\u00");
                    // guaranteed to be 1 or 2 hex digits only
                    buff.append(Character.forDigit(c >>> 4, 16));
                    buff.append(Character.forDigit(c & 15, 16));
                } else {
                    buff.append(c);
                }
            }
        }
    }
