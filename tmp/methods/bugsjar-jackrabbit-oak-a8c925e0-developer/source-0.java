    public void restrictProperty(String propertyName, Operator op, PropertyValue v) {
        PropertyRestriction x = addRestricition(propertyName);
        PropertyValue oldFirst = x.first;
        PropertyValue oldLast = x.last;
        switch (op) {
        case EQUAL:
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
