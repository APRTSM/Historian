    public ExpressionNode visitVariableExpr(final SqlParser.VariableExprContext ctx) {
        VariableExpressionNode node = new VariableExpressionNode();
        return node;
    }
    public void setVariableName(final String variableName) {
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
