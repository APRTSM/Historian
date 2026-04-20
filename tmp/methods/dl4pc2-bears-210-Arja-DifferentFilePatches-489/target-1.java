    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
    public Object getValue(final PredicateContext context) {
        if (context == null)
            return null;
        Map<String, Object> cachedValues = context.getCachedValues();
        Object value = cachedValues.get(variableName);
        return value;
    }
