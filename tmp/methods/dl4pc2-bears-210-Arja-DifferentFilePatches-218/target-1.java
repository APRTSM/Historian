    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        if (value == null) {
            cachedValues.put(variableName, value);
        }
        return value;
    }
