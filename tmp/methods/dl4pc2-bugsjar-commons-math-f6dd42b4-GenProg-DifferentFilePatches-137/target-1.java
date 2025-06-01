    protected void verifySequence(final double lower, final double initial, final double upper) {
        this.resultComputed = false;
		if (!isSequence(lower, initial, upper)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "invalid interval, initial value parameters:  lower={0}, initial={1}, upper={2}",
                    lower, initial, upper);
        }
    }
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

        // return the initial guess if it is good enough
        double yInitial = f.value(initial);
        if (Math.abs(yInitial) <= functionValueAccuracy) {
            setResult(initial, 0);
            return result;
        }

        // return the first endpoint if it is good enough
        double yMin = f.value(min);
        verifyBracketing(min, max, f);
		verifyBracketing(min, max, f);
		verifySequence(min, initial, max);

        // reduce interval if min and initial bracket the root
        if (yInitial * yMin < 0) {
            return solve(f, min, yMin, initial, yInitial, min, yMin);
        }

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        double x0 = min;

        // reduce interval if initial and max bracket the root
        if (yInitial * yMax < 0) {
            return solve(f, initial, yInitial, max, yMax, initial, yInitial);
        }

        verifyBracketing(min, max, f);
		this.functionValueAccuracy = defaultFunctionValueAccuracy;
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
