    protected void incrementIterationsCounter()
        throws MaxIterationsExceededException {
        if (++iterationCount > maximalIterationCount) {
            throw new MaxIterationsExceededException(maximalIterationCount);
        }
    }
