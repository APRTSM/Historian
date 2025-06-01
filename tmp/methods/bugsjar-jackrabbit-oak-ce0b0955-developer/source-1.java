        public String toString() {
            String leftExpr;
            boolean leftExprIsName;
            if (left == null) {
                leftExprIsName = false;
                leftExpr = "";
            } else {
                leftExprIsName = left.isName();
                leftExpr = left.toString();
                if (left.getPrecedence() < precedence) {
                    leftExpr = "(" + leftExpr + ")";
                }
            }
            boolean impossible = false;
            String rightExpr;
            if (right == null) {
                rightExpr = "";
            } else {
                if (left != null && left instanceof Property && ((Property) left).implicitAsterisk) {
                    throw new IllegalArgumentException(
                            "Missing @ in front of the property name: " + left);
                }
                if (leftExprIsName && !"like".equals(operator)) {
                    // need to de-escape _x0020_ and so on
                    if (!(right instanceof Literal)) {
                        throw new IllegalArgumentException(
                                "Can only compare a name against a string literal, not " + right);
                    }
                    Literal l = (Literal) right;
                    String raw = l.rawText;
                    String decoded = ISO9075.decode(raw);
                    String encoded = ISO9075.encode(decoded);
                    rightExpr = SQL2Parser.escapeStringLiteral(decoded);
                    if (!encoded.equalsIgnoreCase(raw)) {
                        // nothing can potentially match
                        impossible = true;
                    }
                } else {
                    rightExpr = right.toString();
                }
                if (right.getPrecedence() < precedence) {
                    rightExpr = "(" + right + ")";
                }
            }
            if (impossible) {
                // a condition that can not possibly be true
                return "upper(" + leftExpr + ") = 'never matches'";
            }
            return (leftExpr + " " + operator + " " + rightExpr).trim();
        }
        public String toString() {
            StringBuilder buff = new StringBuilder("contains").
                    append('(').append(left).append(", ").append(right).append(')');
            return buff.toString();
        }
        Property(Selector selector, String name, boolean implicitAsterisk) {
            this.selector = selector;
            this.name = name;
            this.implicitAsterisk = implicitAsterisk;
        }
    private Expression parsePropertyOrFunction() throws ParseException {
        StringBuilder buff = new StringBuilder();
        boolean isPath = false;
        while (true) {
            if (currentTokenType == IDENTIFIER) {
                String name = readPathSegment();
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
                    buff.append(readPathSegment());
                }
                return new Expression.Property(currentSelector, buff.toString(), false);
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
            return new Expression.Property(currentSelector, buff.toString(), true);
        }
        throw getSyntaxError();
    }
