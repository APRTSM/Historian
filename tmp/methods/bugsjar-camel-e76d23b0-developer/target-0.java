    public static Expression convertToExpression(final Expression expression, final Expression type) {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                Object result = type.evaluate(exchange, Object.class);
                if (result != null) {
                    return expression.evaluate(exchange, result.getClass());
                } else {
                    return expression;
                }
            }

            @Override
            public String toString() {
                return "" + expression;
            }
        };
    }
    public static Expression convertToExpression(final Expression expression, final Class type) {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                if (type != null) {
                    return expression.evaluate(exchange, type);
                } else {
                    return expression;
                }
            }

            @Override
            public String toString() {
                return "" + expression;
            }
        };
    }
