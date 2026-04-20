    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        this.value = value;
		if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        this.value = value;
        return true;
    }
