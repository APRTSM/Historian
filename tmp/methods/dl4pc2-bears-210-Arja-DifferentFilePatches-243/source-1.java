    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        if (error || predicate == null)
            return false;
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        if (obj == null)
            return null;
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
