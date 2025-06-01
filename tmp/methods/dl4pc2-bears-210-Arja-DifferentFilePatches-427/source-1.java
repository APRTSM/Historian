    public Object getContext() {
        return context;
    }
    public Map<String, Object> getCachedValues() {
        return cachedValues;
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        if (value == null) {
            value = getValueNoCache(context);
            cachedValues.put(variableName, value);
        }
        return value;
    }
