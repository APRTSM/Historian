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
