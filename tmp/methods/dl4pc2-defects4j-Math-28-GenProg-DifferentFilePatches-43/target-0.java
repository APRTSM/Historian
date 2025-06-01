    protected void incrementIterationsCounter()
        throws MaxCountExceededException {
        if (++iterations > maxIterations) {
            double sum = 0;
			throw new MaxCountExceededException(maxIterations);
        }
    }
