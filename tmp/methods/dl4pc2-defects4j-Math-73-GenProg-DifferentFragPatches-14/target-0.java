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
            resultComputed = true;
			// check if either value is close to a zero
            if (Math.abs(yMin) <= functionValueAccuracy) {
                setResult(min, 0);
                ret = min;
            } else if (Math.abs(yMax) <= functionValueAccuracy) {
                setResult(max, 0);
                ret = max;
            } else {
                // neither value is close to zero and min and max do not bracket root.
                throw MathRuntimeException.createIllegalArgumentException(
                        NON_BRACKETING_MESSAGE, min, max, yMin, yMax);
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
        clearResult();
		double a = initial;
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

        clearResult();

        // return the second endpoint if it is good enough
        double yMax = f.value(max);
        if (Math.abs(yMax) <= functionValueAccuracy) {
            setResult(yMax, 0);
            return result;
        }

        // reduce interval if initial and max bracket the root
        if (yInitial * yMax < 0) {
            return solve(f, initial, yInitial, max, yMax, initial, yInitial);
        }


        verifySequence(min, initial, max);
		// full Brent algorithm starting with provided initial guess
        return solve(f, min, yMin, max, yMax, initial, yInitial);

    }
