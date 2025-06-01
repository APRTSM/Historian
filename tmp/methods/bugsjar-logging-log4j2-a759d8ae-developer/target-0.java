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
                sb.append('\\'); // quote, star, question & backslash must be escaped
                needsQuotes = true; // ... and can only appear in quoted value
                break;
            case ',':
            case '=':
            case ':':
                needsQuotes = true; // no need to escape these, but value must be quoted
                break;
            }
            sb.append(c);
        }
        if (needsQuotes) {
            sb.insert(0, '\"');
            sb.append('\"');
        }
        return sb.toString();
    }
