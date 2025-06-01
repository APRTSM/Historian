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
		setResult(max, 0);

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        this.iterationCount = iterationCount;
		if (Math.abs(yInitial) <= functionValueAccuracy) {
			setResult(initial, 0);
			return result;
		}
		int i = 0;

        if (Math.abs(yMin) <= functionValueAccuracy) {
			setResult(yMin, 0);
			return result;
		}
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
