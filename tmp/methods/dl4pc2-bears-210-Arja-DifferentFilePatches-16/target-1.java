    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        this.value = value;
		if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        if (rawValue instanceof Boolean)
        	return Boolean.TRUE.equals(rawValue);
        return value.getValue(context) == null;
    }
