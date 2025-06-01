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

        if (expression != null && expression.getExpression() == null) {
            // use toString from predicate or expression so we have some information to show in the route model
            if (expression.getPredicate() != null) {
                expression.setExpression(expression.getPredicate().toString());
            } else if (expression.getExpressionValue() != null) {
                expression.setExpression(expression.getExpressionValue().toString());
            }
        }
    }
    public String getLabel() {
        Predicate predicate = getPredicate();
        if (predicate != null) {
            return predicate.toString();
        }
        Expression expressionValue = getExpressionValue();
        if (expressionValue != null) {
            return expressionValue.toString();
        }

        String exp = getExpression();
        return exp != null ? exp : "";
    }
