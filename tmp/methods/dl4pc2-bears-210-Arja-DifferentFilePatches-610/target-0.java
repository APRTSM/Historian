    public boolean satisfiedBy(final PredicateContext context) throws PredicateExecutionException {
        try {
            return predicate.satisfiedBy(context);
        } catch (Exception ex) {
            throw new PredicateExecutionException("Exception while executing SQL predicate", ex);
        }
    }
