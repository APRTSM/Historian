    public ExpressionNode visitVariableExpr(final SqlParser.VariableExprContext ctx) {
        VariableExpressionNode node = new VariableExpressionNode();
        node.setVariableName(ctx.getText());
        return node;
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        if (value == null) {
            value = getValueNoCache(context);
            cachedValues.put(variableName, value);
        }
        return value;
    }
