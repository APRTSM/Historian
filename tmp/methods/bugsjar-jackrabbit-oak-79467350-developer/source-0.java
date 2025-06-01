    private boolean evaluate(PropertyValue p1, PropertyValue p2) {
        switch (operator) {
        case EQUAL:
            return PropertyValues.match(p1, p2);
        case NOT_EQUAL:
            return !PropertyValues.match(p1, p2);
        case GREATER_OR_EQUAL:
            return p1.compareTo(p2) >= 0;
        case GREATER_THAN:
            return p1.compareTo(p2) > 0;
        case LESS_OR_EQUAL:
            return p1.compareTo(p2) <= 0;
        case LESS_THAN:
            return p1.compareTo(p2) < 0;
        case LIKE:
            return evaluateLike(p1, p2);
        }
        throw new IllegalArgumentException("Unknown operator: " + operator);
    }
