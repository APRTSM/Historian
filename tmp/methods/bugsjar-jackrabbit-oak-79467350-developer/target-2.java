    private boolean evaluate(PropertyValue p1, PropertyValue p2) {
        switch (operator) {
        case EQUAL:
            return PropertyValues.match(p1, p2);
        case NOT_EQUAL:
            return PropertyValues.notMatch(p1, p2);
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
                // we keep the old equality condition if there is one;
                // we can not use setAlwaysFalse, as this would not be correct
                // for multi-valued properties:
                // unlike in databases, "x = 1 and x = 2" can match a node
                // if x is a multi-valued property with value {1, 2}
            } else {
                // all other conditions (range conditions) are replaced with this one
                // (we can not use setAlwaysFalse for the same reason as above)
                x.first = x.last = v;
                x.firstIncluding = x.lastIncluding = true;
            }
            break;
        case NOT_EQUAL:
            if (v != null) {
                throw new IllegalArgumentException("NOT_EQUAL only supported for NOT_EQUAL NULL");
            }
            break;
        case GREATER_THAN:
            // we don't narrow the range because of multi-valued properties
            if (x.first == null) {
                x.first = maxValue(oldFirst, v);
                x.firstIncluding = false;
            }
            break;
        case GREATER_OR_EQUAL:
            // we don't narrow the range because of multi-valued properties
            if (x.first == null) {
                x.first = maxValue(oldFirst, v);
                x.firstIncluding = x.first == oldFirst ? x.firstIncluding : true;
            }
            break;
        case LESS_THAN:
            // we don't narrow the range because of multi-valued properties
            if (x.last == null) {
                x.last = minValue(oldLast, v);
                x.lastIncluding = false;
            }
            break;
        case LESS_OR_EQUAL:
            // we don't narrow the range because of multi-valued properties
            if (x.last == null) {
                x.last = minValue(oldLast, v);
                x.lastIncluding = x.last == oldLast ? x.lastIncluding : true;
            }
            break;
        case LIKE:
            // we don't narrow the range because of multi-valued properties
            if (x.first == null) {
                // LIKE is handled in the fulltext index
                x.isLike = true;
                x.first = v;
            }
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
    public static boolean match(PropertyValue p1, PropertyValue p2) {
        if (p1.getType().tag() != p2.getType().tag()) {
            return false;
        }

        switch (p1.getType().tag()) {
        case PropertyType.BINARY:
            if (p1.isArray() && !p2.isArray()) {
                return contains(p1.getValue(Type.BINARIES),
                        p2.getValue(Type.BINARY));
            }
            if (!p1.isArray() && p2.isArray()) {
                return contains(p2.getValue(Type.BINARIES),
                        p1.getValue(Type.BINARY));
            }
            break;
        default:
            if (p1.isArray() && !p2.isArray()) {
                return contains(p1.getValue(Type.STRINGS),
                        p2.getValue(Type.STRING));
            }
            if (!p1.isArray() && p2.isArray()) {
                return contains(p2.getValue(Type.STRINGS),
                        p1.getValue(Type.STRING));
            }
        }
        // both arrays or both single values
        return p1.compareTo(p2) == 0;

    }
    public static boolean notMatch(PropertyValue p1, PropertyValue p2) {
        if (p1.getType().tag() != p2.getType().tag()) {
            return true;
        }

        switch (p1.getType().tag()) {
        case PropertyType.BINARY:
            if (p1.isArray() && !p2.isArray()) {
                if (p1.count() > 1) {
                    // a value can not possibly match multiple distinct values
                    return true;
                }
                return !contains(p1.getValue(Type.BINARIES),
                        p2.getValue(Type.BINARY));
            }
            if (!p1.isArray() && p2.isArray()) {
                if (p2.count() > 1) {
                    // a value can not possibly match multiple distinct values
                    return true;
                }
                return !contains(p2.getValue(Type.BINARIES),
                        p1.getValue(Type.BINARY));
            }
            break;
        default:
            if (p1.isArray() && !p2.isArray()) {
                if (p1.count() > 1) {
                    // a value can not possibly match multiple distinct values
                    return true;
                }
                return !contains(p1.getValue(Type.STRINGS),
                        p2.getValue(Type.STRING));
            }
            if (!p1.isArray() && p2.isArray()) {
                if (p2.count() > 1) {
                    // a value can not possibly match multiple distinct values
                    return true;
                }
                return !contains(p2.getValue(Type.STRINGS),
                        p1.getValue(Type.STRING));
            }
        }
        // both arrays or both single values
        return p1.compareTo(p2) != 0;

    }
