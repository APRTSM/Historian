    private Expression.Property readProperty() throws ParseException {
        if (readIf("*")) {
            return new Expression.Property(currentSelector, "*");
        }
        return new Expression.Property(currentSelector, readIdentifier());
    }
    private Expression parsePropertyOrFunction() throws ParseException {
        StringBuilder buff = new StringBuilder();
        boolean isPath = false;
        while (true) {
            if (currentTokenType == IDENTIFIER) {
                String name = readIdentifier();
                buff.append(name);
            } else if (readIf("*")) {
                // any node
                buff.append('*');
                isPath = true;
            } else if (readIf(".")) {
                buff.append('.');
                if (readIf(".")) {
                    buff.append('.');
                }
                isPath = true;
            } else if (readIf("@")) {
                if (readIf("*")) {
                    // xpath supports @*, even thought jackrabbit may not
                    buff.append('*');
                } else {
                    buff.append(readIdentifier());
                }
                return new Expression.Property(currentSelector, buff.toString());
            } else {
                break;
            }
            if (readIf("/")) {
                isPath = true;
                buff.append('/');
            } else {
                break;
            }
        }
        if (!isPath && readIf("(")) {
            return parseFunction(buff.toString());
        } else if (buff.length() > 0) {
            // path without all attributes, as in:
            // jcr:contains(jcr:content, 'x')
            if (buff.toString().equals(".")) {
                buff = new StringBuilder("*");
            } else {
                buff.append("/*");
            }
            return new Expression.Property(currentSelector, buff.toString());
        }
        throw getSyntaxError();
    }
