    protected void preCreateProcessor() {
        Expression exp = expression;
        if (expression != null && expression.getExpressionValue() != null) {
            exp = expression.getExpressionValue();
        }

        if (exp instanceof ExpressionClause) {
            ExpressionClause<?> clause = (ExpressionClause<?>) exp;
            if (clause.getExpressionType() != null) {
                // if using the Java DSL then the expression may have been set using the
                // ExpressionClause which is a fancy builder to define expressions and predicates
                // using fluent builders in the DSL. However we need afterwards a callback to
                // reset the expression to the expression type the ExpressionClause did build for us
                expression = clause.getExpressionType();
            }
        }
    }
