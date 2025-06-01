    public static String escape(final String name) {
        final StringBuilder sb = new StringBuilder(name.length() * 2);
        boolean needsQuotes = false;
        for (int i = 0; i < name.length(); i++) {
            final char c = name.charAt(i);
            switch (c) {
            case '\\':
            case '*':
            case '?':
            case '\"':
                // quote, star, question & backslash must be escaped
                sb.append('\\');
                needsQuotes = true; // ... and can only appear in quoted value
                break;
            case ',':
            case '=':
            case ':':
                // no need to escape these, but value must be quoted
                needsQuotes = true;
                break;
            case '\r':
                // replace by \\r, no need to quote
                sb.append("\\r");
                continue;
            case '\n':
                // replace by \\n, no need to quote
                sb.append("\\n");
                continue;
            }
            sb.append(c);
        }
        if (needsQuotes) {
            sb.insert(0, '\"');
            sb.append('\"');
        }
        return sb.toString();
    }
