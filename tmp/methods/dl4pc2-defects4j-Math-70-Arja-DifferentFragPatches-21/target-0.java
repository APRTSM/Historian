    public double solve(double min, double max)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        setResult(max, 0);
		return solve(f, min, max);
    }
    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        return solve(f, min, max);
    }
