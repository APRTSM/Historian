    public FullTextTerm(String propertyName, String text, boolean not, boolean escaped, String boost) {
        this.propertyName = propertyName;
        this.text = text;
        this.not = not;
        this.boost = boost;
        // for testFulltextIntercapSQL
        // filter special characters such as '
        // to make tests pass, for example the
        // FulltextQueryTest.testFulltextExcludeSQL,
        // which searches for:
        // "text ''fox jumps'' -other"
        // (please note the two single quotes instead of
        // double quotes before for and after jumps)
        boolean pattern = false;
        if (escaped) {
            filteredText = text;
        } else {
            StringBuilder buff = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '*') {
                    buff.append('%');
                    pattern = true;
                } else if (c == '?') {
                    buff.append('_');
                    pattern = true;
                } else if (c == '_') {
                    buff.append("\\_");
                    pattern = true;
                } else if (Character.isLetterOrDigit(c) || " +-:&".indexOf(c) >= 0) {
                    buff.append(c);
                }
            }
            this.filteredText = buff.toString().toLowerCase();
        }
        if (pattern) {
            like = new LikePattern("%" + filteredText + "%");
        } else {
            like = null;
        }
    }
