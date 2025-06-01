    FullTextExpression parseTerm() throws ParseException {
        if (parseIndex >= text.length()) {
            throw getSyntaxError("term");
        }
        boolean not = false;
        StringBuilder buff = new StringBuilder();
        char c = text.charAt(parseIndex);
        if (c == '-') {
            if (++parseIndex >= text.length()) {
                throw getSyntaxError("term");
            }
            not = true;
        }
        boolean escaped = false;
        String boost = null;
        if (c == '\"') {
            parseIndex++;
            while (true) {
                if (parseIndex >= text.length()) {
                    throw getSyntaxError("double quote");
                }
                c = text.charAt(parseIndex++);
                if (c == '\\') {
                    escaped = true;
                    if (parseIndex >= text.length()) {
                        throw getSyntaxError("escaped char");
                    }
                    c = text.charAt(parseIndex++);
                    buff.append(c);
                } else if (c == '\"') {
                    if (parseIndex < text.length()) {
                        if (text.charAt(parseIndex) == '^') {
                            boost = "";
                        } else if (text.charAt(parseIndex) != ' ') {
                            throw getSyntaxError("space");
                        }
                    }
                    parseIndex++;
                    break;
                } else {
                    buff.append(c);
                }
            }
        } else if (c == '\'' && FullTextSearchImpl.JACKRABBIT_2_SINGLE_QUOTED_PHRASE) {
            // basically the same as double quote
            parseIndex++;
            while (true) {
                if (parseIndex >= text.length()) {
                    throw getSyntaxError("single quote");
                }
                c = text.charAt(parseIndex++);
                if (c == '\\') {
                    escaped = true;
                    if (parseIndex >= text.length()) {
                        throw getSyntaxError("escaped char");
                    }
                    c = text.charAt(parseIndex++);
                    buff.append(c);
                } else if (c == '\'') {
                    if (parseIndex < text.length()) {
                        if (text.charAt(parseIndex) == '^') {
                            boost = "";
                        } else if (text.charAt(parseIndex) != ' ') {
                            throw getSyntaxError("space");
                        }
                    }
                    parseIndex++;
                    break;
                } else {
                    buff.append(c);
                }
            }
        } else {
            do {
                c = text.charAt(parseIndex++);
                if (c == '\\') {
                    escaped = true;
                    if (parseIndex >= text.length()) {
                        throw getSyntaxError("escaped char");
                    }
                    c = text.charAt(parseIndex++);
                    buff.append(c);
                } else if (c == '^') {
                    boost = "";
                    break;
                } else if (c == ' ') {
                    break;
                } else {
                    buff.append(c);
                }
            } while (parseIndex < text.length());
        }
        if (boost != null) {
            StringBuilder b = new StringBuilder();
            while (parseIndex < text.length()) {
                c = text.charAt(parseIndex++);
                if ((c < '0' || c > '9') && c != '.') {
                    break;
                }
                b.append(c);
            }
            boost = b.toString();
        }
        if (buff.length() == 0) {
            throw getSyntaxError("term");
        }
        String text = buff.toString();
        FullTextTerm term = new FullTextTerm(propertyName, text, not, escaped, boost);
        return term.simplify();
    }
