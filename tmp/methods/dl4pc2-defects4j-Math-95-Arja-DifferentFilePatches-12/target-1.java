    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    public double inverseCumulativeProbability(final double p)
        throws MathException {
        if (p <= 0) {
				throw new IllegalArgumentException(
						"The Poisson mean must be positive");
			}
		if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("p must be between 0.0 and 1.0, inclusive.");
        }

        // by default, do simple root finding using bracketing and default solver.
        // subclasses can overide if there is a better method.
        UnivariateRealFunction rootFindingFunction =
            new UnivariateRealFunction() {

            public double value(double x) throws FunctionEvaluationException {
                try {
                    return cumulativeProbability(x) - p;
                } catch (MathException ex) {
                    throw new FunctionEvaluationException(x, ex.getPattern(), ex.getArguments(), ex);
                }
            }
        };
              
        // Try to bracket root, test domain endoints if this fails     
        double lowerBound = getDomainLowerBound(p);
        double upperBound = getDomainUpperBound(p);
        double[] bracket = null;
        try {
            bracket = UnivariateRealSolverUtils.bracket(
                    rootFindingFunction, getInitialDomain(p),
                    lowerBound, upperBound);
        }  catch (ConvergenceException ex) {
            /* 
             * Check domain endpoints to see if one gives value that is within
             * the default solver's defaultAbsoluteAccuracy of 0 (will be the
             * case if density has bounded support and p is 0 or 1).
             * 
             * TODO: expose the default solver, defaultAbsoluteAccuracy as
             * a constant.
             */ 
            if (Math.abs(rootFindingFunction.value(lowerBound)) < 1E-6) {
                return lowerBound;
            }
            if (Math.abs(rootFindingFunction.value(upperBound)) < 1E-6) {
                return upperBound;
            }     
            // Failed bracket convergence was not because of corner solution
            throw new MathException(ex);
        }

        // find root
        double root = UnivariateRealSolverUtils.solve(rootFindingFunction,
                bracket[0],bracket[1]);
        return root;
    }
