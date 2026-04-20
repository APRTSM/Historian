        Property(Selector selector, String name, boolean thereWasNoAt) {
            this.selector = selector;
            this.name = name;
            this.thereWasNoAt = thereWasNoAt;
        }
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
            StringBuilder buff = new StringBuilder("contains(");
            Expression l = left;
            if (l instanceof Property) {
                Property p = (Property) l;
                if (p.thereWasNoAt) {
                    l = new Property(p.selector, p.name + "/*", true);
                }
            }
            buff.append(l);
            buff.append(", ").append(right).append(')');
            return buff.toString();
        }
