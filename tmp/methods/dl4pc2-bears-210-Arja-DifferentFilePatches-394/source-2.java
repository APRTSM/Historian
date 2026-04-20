    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        if (rawValue instanceof Boolean)
        	return Boolean.TRUE.equals(rawValue);
        return true;
    }
    public Object getContext() {
        return context;
    }
    public ExpressionNode visitVariableExpr(final SqlParser.VariableExprContext ctx) {
        VariableExpressionNode node = new VariableExpressionNode();
        node.setVariableName(ctx.getText());
        return node;
    }
