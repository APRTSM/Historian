    protected void incrementIterationsCounter()
        throws ConvergenceException {
        if (++iterationCount > maximalIterationCount) {
            throw new ConvergenceException(new MaxIterationsExceededException(maximalIterationCount));
        }
    }
    protected abstract double doOptimize();
