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
