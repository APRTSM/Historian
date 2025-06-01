    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        if (error || predicate == null)
            return false;
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
    public boolean satisfiedBy(final PredicateContext context) {
        Object rawValue = value.getValue(context);
        if (rawValue == null)
        	return false;
        if (rawValue instanceof String)
        	return !((String)rawValue).isEmpty();
        if (rawValue instanceof Number)
        	return ((Number) rawValue).doubleValue() != 0.0;
        if (rawValue instanceof Boolean)
        	return Boolean.TRUE.equals(rawValue);
        return true;
    }
    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        if (obj == null)
            return null;
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
