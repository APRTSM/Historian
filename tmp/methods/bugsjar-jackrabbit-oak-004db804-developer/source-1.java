        AndCondition(Expression left, Expression right) {
            super(left, "and", right, Expression.PRECEDENCE_AND);
        }
        public String getCommonLeftPart() {
            if (!"=".equals(operator)) {
                return null;
            }
            return left.toString();
        }
        boolean isCondition() {
            return true;
        }
        public String getCommonLeftPart() {
            if (left instanceof Condition && right instanceof Condition) {
                String l = ((Condition) left).getCommonLeftPart();
                String r = ((Condition) right).getCommonLeftPart();
                if (l != null && r != null && l.equals(r)) {
                    return l;
                }
            }
            return null;
        }
        boolean isCondition() {
            return true;
        }
    public Statement optimize() {
        if (explain || measure || orderList.size() > 0) {
            return this;
        }
        if (where == null) {
            return this;
        }
        ArrayList<Expression> unionList = new ArrayList<Expression>();
        addToUnionList(where, unionList);
        if (unionList.size() == 1) {
            return this;
        }
        Statement union = null;
        for (int i = 0; i < unionList.size(); i++) {
            Expression e = unionList.get(i);
            Statement s = new Statement();
            s.columnSelector = columnSelector;
            s.selectors = selectors;
            s.columnList = columnList;
            s.where = e;
            if (i == unionList.size() - 1) {
                s.xpathQuery = xpathQuery;
            }
            if (union == null) {
                union = s;
            } else {
                union = new UnionStatement(union.optimize(), s.optimize());
            }
        }
        return union;
    }
    private static void addToUnionList(Expression condition,  ArrayList<Expression> unionList) {
        if (condition instanceof OrCondition) {
            OrCondition or = (OrCondition) condition;
            if (or.getCommonLeftPart() != null) {
                // @x = 1 or @x = 2 
                // is automatically converted to 
                // @x in (1, 2)
                // within the query engine
            } else if (or.left instanceof Contains && or.right instanceof Contains) {
                // do not optimize "contains"
            } else {
                // conditions of type                
                // @x = 1 or @y = 2
                // or similar are converted to
                // (@x = 1) union (@y = 2)
                addToUnionList(or.left, unionList);
                addToUnionList(or.right, unionList);
                return;
            }
        } else if (condition instanceof AndCondition) {
            // conditions of type
            // @a = 1 and (@x = 1 or @y = 2)
            // are automatically converted to
            // (@a = 1 and @x = 1) union (@a = 1 and @y = 2)
            AndCondition and = (AndCondition) condition;
            and = and.pullOrRight();
            if (and.right instanceof OrCondition) {
                OrCondition or = (OrCondition) and.right;
                if (or.getCommonLeftPart() != null) {
                    // @x = 1 or @x = 2 
                    // is automatically converted to 
                    // @x in (1, 2)
                    // within the query engine                
                } else if (or.left instanceof Contains && or.right instanceof Contains) {
                    // do not optimize "contains"
                } else {
                    // same as above, but with the added "and"
                    addToUnionList(new AndCondition(and.left, or.left), unionList);
                    addToUnionList(new AndCondition(and.left, or.right), unionList);
                    return;
                }
            }
        }
        unionList.add(condition);
    }
        public String toString() {
            return s1 + " union " + s2;
        }
