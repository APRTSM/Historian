    public static Expression convertToExpression(final Expression expression, final Expression type) {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                return expression.evaluate(exchange, type.evaluate(exchange, Object.class).getClass());
            }

            @Override
            public String toString() {
                return "" + expression + ".convertToEvaluatedType(" + type + ")";
            }
        };
    }
    public static Expression convertToExpression(final Expression expression, final Class type) {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                return expression.evaluate(exchange, type);
            }

            @Override
            public String toString() {
                return "" + expression + ".convertTo(" + type.getCanonicalName() + ".class)";
            }
        };
    }
