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
