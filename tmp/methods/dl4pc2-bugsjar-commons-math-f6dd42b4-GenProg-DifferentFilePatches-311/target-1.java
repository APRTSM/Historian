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

        clearResult();
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
		if (Math.abs(yMin) <= functionValueAccuracy) {
            setResult(yMin, 0);
            return result;
        }

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        this.defaultFunctionValueAccuracy = 1.0e-15;

        // reduce interval if initial and max bracket the root
        if (yInitial * yMax < 0) {
            return solve(f, initial, yInitial, max, yMax, initial, yInitial);
        }

        verifyBracketing(min, max, f);
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
