    public static Expression outHeadersExpression() {
        return new ExpressionAdapter() {
            public Object evaluate(Exchange exchange) {
                // only get out headers if the MEP is out capable
                if (ExchangeHelper.isOutCapable(exchange)) {
                    return exchange.getOut().getHeaders();
                } else {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "outHeaders";
            }
        };
    }
