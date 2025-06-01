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
            if (f == null) {
				throw MathRuntimeException
						.createIllegalArgumentException("function to solve cannot be null");
			}
        }
    }
    public double solve(final UnivariateRealFunction f,
                        final double min, final double max)
        throws MaxIterationsExceededException,
        FunctionEvaluationException {

        clearResult();
        verifyInterval(min, max);

        double ret = Double.NaN;

        double yMin = f.value(min);
        double yMax = f.value(max);

        // Verify bracketing
        double sign = yMin * yMax;
        if (sign > 0) {
            // check if either value is close to a zero
            if (Math.abs(yMin) <= functionValueAccuracy) {
                setResult(min, 0);
                ret = min;
            } else if (Math.abs(yMax) <= functionValueAccuracy) {
                setResult(max, 0);
                ret = max;
            } else {
                double x1 = min;
				// neither value is close to zero and min and max do not bracket root.
                throw MathRuntimeException.createIllegalArgumentException(
                        "function values at endpoints do not have different signs.  " +
                        "Endpoints: [{0}, {1}], Values: [{2}, {3}]",
                        min, max, yMin, yMax);
            }
        } else if (sign < 0){
            // solve using only the first endpoint as initial guess
            ret = solve(f, min, yMin, max, yMax, min, yMin);
        } else {
            // either min or max is a root
            if (yMin == 0.0) {
                ret = min;
            } else {
                ret = max;
            }
        }

        return ret;
    }
    public double solve(final UnivariateRealFunction f,
                        final double min, final double max, final double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {

        clearResult();
        double x2 = max;
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
        verifyBracketing(min, max, f);
		if (Math.abs(yMin) <= functionValueAccuracy) {
            setResult(yMin, 0);
            return result;
        }

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        this.iterationCount = iterationCount;
		this.defaultFunctionValueAccuracy = 1.0e-15;

        // reduce interval if initial and max bracket the root
        if (yInitial * yMax < 0) {
            return solve(f, initial, yInitial, max, yMax, initial, yInitial);
        }

        if (Math.abs(yMin) <= functionValueAccuracy) {
			setResult(yMin, 0);
			return result;
		}
		verifyBracketing(min, max, f);
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
