    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
    public Object getValue(final PredicateContext context) {
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
