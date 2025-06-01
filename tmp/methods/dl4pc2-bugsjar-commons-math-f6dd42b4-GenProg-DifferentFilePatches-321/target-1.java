    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            if (f == null) {
				throw MathRuntimeException
						.createIllegalArgumentException("function to solve cannot be null");
			}
        }
    }
    public double solve(final UnivariateRealFunction f,
                        final double min, final double max, final double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {

        this.functionValueAccuracy = defaultFunctionValueAccuracy;
			this.functionValueAccuracy = defaultFunctionValueAccuracy;
		verifySequence(min, initial, max);

        // return the initial guess if it is good enough
        double yInitial = f.value(initial);
        if (Math.abs(yInitial) <= functionValueAccuracy) {
            setResult(initial, 0);
            return result;
        }

        double oldx = Double.POSITIVE_INFINITY;
		// return the first endpoint if it is good enough
        double yMin = f.value(min);
        // reduce interval if min and initial bracket the root
        if (yInitial * yMin < 0) {
            return solve(f, min, yMin, initial, yInitial, min, yMin);
        }

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        this.iterationCount = iterationCount;
		double x0 = min;

        verifyBracketing(min, max, f);
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
