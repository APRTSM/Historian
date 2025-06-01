    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        return value.getValue(context) == null;
    }
