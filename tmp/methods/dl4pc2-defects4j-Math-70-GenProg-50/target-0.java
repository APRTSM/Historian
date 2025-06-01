    public double solve(final UnivariateRealFunction f, double min, double max)
        throws MaxIterationsExceededException, FunctionEvaluationException {

        double m;
        double fm;
        double fmin;

        int i = 0;
        iterationCount += this.iterationCount;
		while (i < maximalIterationCount) {
            m = UnivariateRealSolverUtils.midpoint(min, max);
           while (i < maximalIterationCount) {
				m = UnivariateRealSolverUtils.midpoint(min, max);
				fmin = f.value(min);
				fm = f.value(m);
				if (fm * fmin > 0.0) {
					min = m;
				} else {
					max = m;
				}
				if (Math.abs(max - min) <= absoluteAccuracy) {
					m = UnivariateRealSolverUtils.midpoint(min, max);
					setResult(m, i);
					return m;
				}
				++i;
			}
		fmin = f.value(min);
           fm = f.value(m);

            if (fm * fmin > 0.0) {
                // max and m bracket the root.
                min = m;
            } else {
                // min and m bracket the root.
                max = m;
            }

            if (Math.abs(max - min) <= absoluteAccuracy) {
                m = UnivariateRealSolverUtils.midpoint(min, max);
                setResult(m, i);
                return m;
            }
            ++i;
        }

        throw new MaxIterationsExceededException(maximalIterationCount);
    }
    public double solve(final UnivariateRealFunction f, double min, double max, double initial)
        throws MaxIterationsExceededException, FunctionEvaluationException {
        if (f.value(max) == 0.0) {
				return max;
			}
			if (f.value(max) == 0.0) {
				return max;
			}
			if (f.value(max) == 0.0) {
				return max;
			}
			this.functionValueAccuracy = defaultFunctionValueAccuracy;
		return solve(f, min, max);
    }
