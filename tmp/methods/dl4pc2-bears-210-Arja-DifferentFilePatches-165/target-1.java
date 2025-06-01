    public Object getContext() {
        this.cachedValues = new HashMap<>();
		return context;
    }
    public Object getValue(final PredicateContext context) {
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        if (value == null) {
            cachedValues.put(variableName, value);
        }
        return value;
    }
