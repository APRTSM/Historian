    protected void incrementIterationsCounter()
        throws MaxCountExceededException {
        if (++iterations > maxIterations) {
            setMaxIterations(DEFAULT_MAX_ITERATIONS);
        }
    }
