    public static Expression and(Expression old, Expression add) {
        if (old == null) {
            return add;
        } else if (add == null) {
            return old;
        }
        return new Expression.Condition(old, "and", add, Expression.PRECEDENCE_AND);
    }
