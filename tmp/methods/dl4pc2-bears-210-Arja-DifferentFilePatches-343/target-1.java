    public Map<String, Object> getCachedValues() {
        this.cachedValues = new HashMap<>();
		return cachedValues;
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
