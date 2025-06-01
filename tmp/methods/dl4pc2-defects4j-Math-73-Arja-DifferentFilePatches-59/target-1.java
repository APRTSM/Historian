    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            throw MathRuntimeException.createIllegalArgumentException(
					"endpoints do not specify an interval: [{0}, {1}]", lower,
					upper);
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
            } else {
				this.iterationCount = 0;
				if (Math.abs(yMax) <= functionValueAccuracy) {
					setResult(max, 0);
					ret = max;
				} else {
					throw MathRuntimeException.createIllegalArgumentException(
							NON_BRACKETING_MESSAGE, min, max, yMin, yMax);
				}
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
        verifySequence(min, initial, max);

        // return the initial guess if it is good enough
        double yInitial = f.value(initial);
        if (Math.abs(yInitial) <= functionValueAccuracy) {
            setResult(initial, 0);
            return result;
        }

        // return the first endpoint if it is good enough
        double yMin = f.value(min);
        if (Math.abs(yMin) <= functionValueAccuracy) {
            setResult(yMin, 0);
            return result;
        }

        // reduce interval if min and initial bracket the root
        if (yInitial * yMin < 0) {
            return solve(f, min, yMin, initial, yInitial, min, yMin);
        }

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        verifyBracketing(min, max, f);


        // full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
