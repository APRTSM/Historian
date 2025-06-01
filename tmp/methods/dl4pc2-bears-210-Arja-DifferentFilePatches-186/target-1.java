    public boolean satisfiedBy(final PredicateContext context) {
        this.value = value;
		Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        this.value = value;
        return true;
    }
    public Object getContext() {
        this.cachedValues = new HashMap<>();
		return context;
    }
