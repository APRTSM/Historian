    Expression pullOrRight() {
        return this;
    }
        AndCondition pullOrRight() {
            if (right instanceof OrCondition) {
                return this;
            } else if (left instanceof OrCondition) {
                return new AndCondition(right, left);
            }
            if (right instanceof AndCondition) {
                // pull up x:
                // a and (b and (x)) -> (a and b) and (x)
                AndCondition r2 = (AndCondition) right;
                r2 = r2.pullOrRight();
                AndCondition l2 = new AndCondition(left, r2.left);
                l2 = l2.pullOrRight();
                return new AndCondition(l2, r2.right);
            } else if (left instanceof AndCondition) {
                return new AndCondition(right, left).pullOrRight();
            }
            return this;
        }
    public static Expression and(Expression old, Expression add) {
        if (old == null) {
            return add;
        } else if (add == null) {
            return old;
        }
        return new Expression.AndCondition(old, add);
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
