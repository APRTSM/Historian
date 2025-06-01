    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
    public static Object getValue(final Object obj, final @NonNull String variableName)
            throws ReflectiveOperationException {
        return PropertyUtils.getNestedProperty(obj, variableName);
    }
