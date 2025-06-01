    public static Expression and(Expression old, Expression add) {
        if (old == null) {
            return add;
        } else if (add == null) {
            return old;
        }
        return new Expression.Condition(old, "and", add, Expression.PRECEDENCE_AND);
    }
    public Statement optimize() {
        if (explain || measure || orderList.size() > 0) {
            return this;
        }
        if (where == null) {
            return this;
        }
        if (where instanceof OrCondition) {
            OrCondition or = (OrCondition) where;
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
                Statement s1 = new Statement();
                s1.columnSelector = columnSelector;
                s1.selectors = selectors;
                s1.columnList = columnList;
                s1.where = or.left;
                Statement s2 = new Statement();
                s2.columnSelector = columnSelector;
                s2.selectors = selectors;
                s2.columnList = columnList;
                s2.where = or.right;
                s2.xpathQuery = xpathQuery;
                return new UnionStatement(s1.optimize(), s2.optimize());
            }
        } else if (where instanceof AndCondition) {
            // conditions of type
            // @a = 1 and (@x = 1 or @y = 2)
            // are automatically converted to
            // (@a = 1 and @x = 1) union (@a = 1 and @y = 2)
            AndCondition and = (AndCondition) where;
            if (and.left instanceof OrCondition && !(and.right instanceof OrCondition)) {
                // swap left and right
                and = new AndCondition(and.right, and.left);
            }
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
                    // TODO avoid code duplication if possible
                    Statement s1 = new Statement();
                    s1.columnSelector = columnSelector;
                    s1.selectors = selectors;
                    s1.columnList = columnList;
                    s1.where = new AndCondition(and.left, or.left);
                    Statement s2 = new Statement();
                    s2.columnSelector = columnSelector;
                    s2.selectors = selectors;
                    s2.columnList = columnList;
                    s2.where = new AndCondition(and.left, or.right);
                    s2.xpathQuery = xpathQuery;
                    return new UnionStatement(s1.optimize(), s2.optimize());
                }
            }
        }
        return this;
    }
