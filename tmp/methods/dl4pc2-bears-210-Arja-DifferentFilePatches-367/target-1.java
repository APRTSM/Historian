    public Map<String, Object> getCachedValues() {
        this.context = context;
		return cachedValues;
    }
    public void setVariableName(final String variableName) {
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
