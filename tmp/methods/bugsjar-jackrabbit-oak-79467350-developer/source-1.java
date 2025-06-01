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
    public void restrictProperty(String propertyName, Operator op, PropertyValue v) {
        PropertyRestriction x = addRestricition(propertyName);
        PropertyValue oldFirst = x.first;
        PropertyValue oldLast = x.last;
        switch (op) {
        case EQUAL:
            if (x.first != null && x.last == x.first && x.firstIncluding && x.lastIncluding) {
                // there is already an equality condition on this property
                // we will keep this, as it could be a multi-valued property
                // (unlike in databases, "x = 1 and x = 2" can match a node
                // if x is a multi-valued property with value "{1, 2}")
                return;
            }
            x.first = maxValue(oldFirst, v);
            x.firstIncluding = x.first == oldFirst ? x.firstIncluding : true;
            x.last = minValue(oldLast, v);
            x.lastIncluding = x.last == oldLast ? x.lastIncluding : true;
            break;
        case NOT_EQUAL:
            if (v != null) {
                throw new IllegalArgumentException("NOT_EQUAL only supported for NOT_EQUAL NULL");
            }
            break;
        case GREATER_THAN:
            x.first = maxValue(oldFirst, v);
            x.firstIncluding = false;
            break;
        case GREATER_OR_EQUAL:
            x.first = maxValue(oldFirst, v);
            x.firstIncluding = x.first == oldFirst ? x.firstIncluding : true;
            break;
        case LESS_THAN:
            x.last = minValue(oldLast, v);
            x.lastIncluding = false;
            break;
        case LESS_OR_EQUAL:
            x.last = minValue(oldLast, v);
            x.lastIncluding = x.last == oldLast ? x.lastIncluding : true;
            break;
        case LIKE:
            // LIKE is handled in the fulltext index
            x.isLike = true;
            x.first = v;
            break;
        case IN:
            
        }
        if (x.first != null && x.last != null) {
            if (x.first.compareTo(x.last) > 0) {
                setAlwaysFalse();
            } else if (x.first.compareTo(x.last) == 0 && (!x.firstIncluding || !x.lastIncluding)) {
                setAlwaysFalse();
            }
        }
    }
