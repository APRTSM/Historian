    private static String rewriteQueryText(String textsearch) {
        // replace escaped ' with just '
        StringBuilder rewritten = new StringBuilder();
        // the default lucene query parser recognizes 'AND' and 'NOT' as
        // keywords.
        textsearch = textsearch.replaceAll("AND", "and");
        textsearch = textsearch.replaceAll("NOT", "not");
        boolean escaped = false;
        for (int i = 0; i < textsearch.length(); i++) {
            if (textsearch.charAt(i) == '\\') {
                if (escaped) {
                    rewritten.append("\\\\");
                    escaped = false;
                } else {
                    escaped = true;
                }
            } else if (textsearch.charAt(i) == '\'') {
                if (escaped) {
                    escaped = false;
                }
                rewritten.append(textsearch.charAt(i));
            } else if (textsearch.charAt(i) == ':') {
                // fields as known in lucene are not supported
                rewritten.append("\\:");
            } else {
                if (escaped) {
                    rewritten.append('\\');
                    escaped = false;
                }
                rewritten.append(textsearch.charAt(i));
            }
        }
        return rewritten.toString();
    }
