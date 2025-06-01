    static String rewriteQueryText(String textsearch) {
        // replace escaped ' with just '
        StringBuilder rewritten = new StringBuilder();
        // the default lucene query parser recognizes 'AND' and 'NOT' as
        // keywords.
        textsearch = textsearch.replaceAll("AND", "and");
        textsearch = textsearch.replaceAll("NOT", "not");
        boolean escaped = false;
        for (int i = 0; i < textsearch.length(); i++) {
            char c = textsearch.charAt(i);
            if (c == '\\') {
                if (escaped) {
                    rewritten.append("\\\\");
                    escaped = false;
                } else {
                    escaped = true;
                }
            } else if (c == '\'') {
                if (escaped) {
                    escaped = false;
                }
                rewritten.append(c);
            } else if (c == ':' || c == '/') {
                //TODO Some other chars are also considered special See OAK-3769 for details
                //':' fields as known in lucene are not supported
                //'/' its a special char used for regex search in Lucene
                rewritten.append('\\').append(c);
            } else {
                if (escaped) {
                    rewritten.append('\\');
                    escaped = false;
                }
                rewritten.append(c);
            }
        }
        return rewritten.toString();
    }
