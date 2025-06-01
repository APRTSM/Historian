    public double solve(double min, double max)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        if (f.value(max) == 0.0) {
				return max;
			}
		return solve(f, min, max);
    }
    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        return solve(f, min, max);
    }
