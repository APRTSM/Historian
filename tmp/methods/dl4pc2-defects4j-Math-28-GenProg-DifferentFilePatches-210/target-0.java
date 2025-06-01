    protected void incrementIterationsCounter()
        throws MaxCountExceededException {
        if (++iterations > maxIterations) {
            if (++iterations > maxIterations) {
				throw new MaxCountExceededException(maxIterations);
			}
			throw new MaxCountExceededException(maxIterations);
        }
    }
