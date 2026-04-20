    public Object getValue(final PredicateContext context) {
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
