    protected void incrementIterationsCounter()
        throws OptimizationException {
        if (++iterations > maxIterations) {
            throw new OptimizationException(new MaxIterationsExceededException(maxIterations));
        }
    }
