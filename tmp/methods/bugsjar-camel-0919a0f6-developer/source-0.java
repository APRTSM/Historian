    public static Expression outHeadersExpression() {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                return exchange.getOut().getHeaders();
            }

            @Override
            public String toString() {
                return "outHeaders";
            }
        };
    }
