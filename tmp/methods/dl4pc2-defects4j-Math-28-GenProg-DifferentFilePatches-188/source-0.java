    protected void incrementIterationsCounter()
        throws MaxCountExceededException {
        if (++iterations > maxIterations) {
            throw new MaxCountExceededException(maxIterations);
        }
    }
