    public boolean satisfiedBy(final PredicateContext context) {
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
    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
