    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            if (lower >= upper) {
				throw MathRuntimeException.createIllegalArgumentException(
						"endpoints do not specify an interval: [{0}, {1}]",
						lower, upper);
			}
        }
    }
    public double solve(final UnivariateRealFunction f,
                        final double min, final double max, final double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {

        verifySequence(min, initial, max);
		verifySequence(min, initial, max);

        // return the initial guess if it is good enough
        double yInitial = f.value(initial);
        if (Math.abs(yInitial) <= functionValueAccuracy) {
            setResult(initial, 0);
            return result;
        }

        // return the first endpoint if it is good enough
        double yMin = f.value(min);
        verifyBracketing(min, max, f);
		if (yInitial * yMin < 0) {
			return solve(f, min, yMin, initial, yInitial, min, yMin);
		}

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        this.iterationCount = iterationCount;
		this.iterationCount = iterationCount;
		this.iterationCount = iterationCount;
		double x0 = min;

        // reduce interval if initial and max bracket the root
        if (yInitial * yMax < 0) {
            return solve(f, initial, yInitial, max, yMax, initial, yInitial);
        }

        verifyBracketing(min, max, f);
		verifyBracketing(min, max, f);
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
