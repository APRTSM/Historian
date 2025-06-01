    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        if (rawValue instanceof Boolean)
        	return Boolean.TRUE.equals(rawValue);
        if (rawValue instanceof Collection)
		return !((Collection<?>) rawValue).isEmpty();
        if (rawValue instanceof Object[])
		return ((Object[]) rawValue).length != 0;
        return true;
    }
