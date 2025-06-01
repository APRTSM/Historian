    public double inverseCumulativeProbability(final double p) throws OutOfRangeException {

        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        // by default, do simple root finding using bracketing and default solver.
        // subclasses can override if there is a better method.
        UnivariateFunction rootFindingFunction =
            new UnivariateFunction() {
            public double value(double x) {
                return cumulativeProbability(x) - p;
            }
        };

        // Try to bracket root, test domain endpoints if this fails
        double lowerBound = getDomainLowerBound(p);
        double upperBound = getDomainUpperBound(p);
        double[] bracket = null;
        try {
            bracket = UnivariateRealSolverUtils.bracket(
                    rootFindingFunction, getInitialDomain(p),
                    lowerBound, upperBound);
        } catch (NumberIsTooLargeException ex) {
            /*
             * Check domain endpoints to see if one gives value that is within
             * the default solver's defaultAbsoluteAccuracy of 0 (will be the
             * case if density has bounded support and p is 0 or 1).
             */
            if (FastMath.abs(rootFindingFunction.value(lowerBound)) < getSolverAbsoluteAccuracy()) {
                return lowerBound;
            }
            if (FastMath.abs(rootFindingFunction.value(upperBound)) < getSolverAbsoluteAccuracy()) {
                return upperBound;
            }
            // Failed bracket convergence was not because of corner solution
            throw new MathInternalError(ex);
        }

        // find root
        double root = UnivariateRealSolverUtils.solve(rootFindingFunction,
                // override getSolverAbsoluteAccuracy() to use a Brent solver with
                // absolute accuracy different from the default.
                bracket[0],bracket[1], getSolverAbsoluteAccuracy());
        return root;
    }
