    protected void incrementIterationsCounter()
        throws MaxCountExceededException {
        if (++iterations > maxIterations) {
            this.maxIterations = maxIterations;
			throw new MaxCountExceededException(maxIterations);
        }
    }
