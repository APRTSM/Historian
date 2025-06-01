    String getCommonLeftPart() {
        return null;
    }
        boolean containsFullTextCondition() {
            return left.containsFullTextCondition() || right.containsFullTextCondition();
        }
        public String getCommonLeftPart() {
            String l = left.getCommonLeftPart();
            String r = right.getCommonLeftPart();
            if (l != null && r != null && l.equals(r)) {
                return l;
            }
            return null;
        }
        boolean containsFullTextCondition() {
            return true;
        }
        Expression getLeft() {
            return left;
        }
        InCondition(Expression left, List<Expression> list) {
            this.left = left;
            this.list = list;
        }
        Expression optimize() {
            Expression l = left.optimize();
            Expression r = right.optimize();
            if (l != left || r != right) {
                return new OrCondition(l, r).optimize();
            }
            String commonLeft = getCommonLeftPart();
            if (commonLeft == null) {
                return this;
            }
            // "@x = 1 or @x = 2" is converted to "@x in (1, 2)"
            ArrayList<Expression> list = new ArrayList<Expression>();
            list.addAll(left.getRight());
            list.addAll(right.getRight());
            Expression le = left.getLeft();
            InCondition in = new InCondition(le, list);
            return in.optimize();
        }
        List<Expression> getRight() {
            return Collections.singletonList(right);
        }
        boolean containsFullTextCondition() {
            return left.containsFullTextCondition() || right.containsFullTextCondition();
        }
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append(left).append(" in(");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    buff.append(", ");
                }
                buff.append(list.get(i));
            }
            return buff.append(')').toString();
        }
        String getCommonLeftPart() {
            if (!"=".equals(operator)) {
                return null;
            }
            return left.toString();
        }
    boolean containsFullTextCondition() {
        return false;
    }
        boolean containsFullTextCondition() {
            return true;
        }
        Expression optimize() {
            Expression l = left.optimize();
            Expression r = right.optimize();
            if (l != left || r != right) {
                return new AndCondition(l, r);
            }
            return this;
        }
        String getCommonLeftPart() {
            return left.toString();
        }
        boolean isCondition() {
            return true;
        }
    Expression getLeft() {
        return null;
    }
        Expression getLeft() {
            return left;
        }
    List<Expression> getRight() {
        return null;
    }
    Expression optimize() {
        return this;
    }
        Expression optimize() {
            return this;
        }
        List<Expression> getRight() {
            return list;
        }
    public Statement optimize() {
        if (explain || measure) {
            return this;
        }
        if (where == null) {
            return this;
        }
        where = where.optimize();
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
            if (union == null) {
                union = s;
            } else {
                union = new UnionStatement(union.optimize(), s.optimize());
            }
        }
        union.orderList = orderList;
        union.xpathQuery = xpathQuery;
        return union;
    }
    private static void addToUnionList(Expression condition,  ArrayList<Expression> unionList) {
        if (condition.containsFullTextCondition()) {
            // do not use union
        } else if (condition instanceof OrCondition) {
            OrCondition or = (OrCondition) condition;
            // conditions of type
            // @x = 1 or @y = 2
            // or similar are converted to
            // (@x = 1) union (@y = 2)
            addToUnionList(or.left, unionList);
            addToUnionList(or.right, unionList);
            return;
        } else if (condition instanceof AndCondition) {
            // conditions of type
            // @a = 1 and (@x = 1 or @y = 2)
            // are automatically converted to
            // (@a = 1 and @x = 1) union (@a = 1 and @y = 2)
            AndCondition and = (AndCondition) condition;
            and = and.pullOrRight();
            if (and.right instanceof OrCondition) {
                OrCondition or = (OrCondition) and.right;
                // same as above, but with the added "and"
                addToUnionList(new AndCondition(and.left, or.left), unionList);
                addToUnionList(new AndCondition(and.left, or.right), unionList);
                return;
            }
        }
        unionList.add(condition);
    }
        public String toString() {
            StringBuilder buff = new StringBuilder();
            buff.append(s1).append(" union ").append(s2);
            // order by ...
            if (orderList != null && !orderList.isEmpty()) {
                buff.append(" order by ");
                for (int i = 0; i < orderList.size(); i++) {
                    if (i > 0) {
                        buff.append(", ");
                    }
                    buff.append(orderList.get(i));
                }
            }
            // leave original xpath string as a comment
            if (xpathQuery != null) {
                buff.append(" /* xpath: ");
                buff.append(xpathQuery);
                buff.append(" */");
            }
            return buff.toString();
        }
