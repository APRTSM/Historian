    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        this.value = value;
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        if (rawValue instanceof Boolean)
        	return Boolean.TRUE.equals(rawValue);
        return value.getValue(context) == null;
    }
    public Object getContext() {
        this.cachedValues = new HashMap<>();
		return context;
    }
