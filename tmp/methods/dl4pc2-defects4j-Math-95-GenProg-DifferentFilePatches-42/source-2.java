    public double inverseCumulativeProbability(final double p)
        throws MathException {
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
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return ret;
    }
    public static double[] bracket(UnivariateRealFunction function,
            double initial, double lowerBound, double upperBound, 
            int maximumIterations) throws ConvergenceException, 
            FunctionEvaluationException {
        
        if (function == null) {
            throw new IllegalArgumentException ("function is null.");
        }
        if (maximumIterations <= 0)  {
            throw new IllegalArgumentException
            ("bad value for maximumIterations: " + maximumIterations);
        }
        if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
            throw new IllegalArgumentException
            ("Invalid endpoint parameters:  lowerBound=" + lowerBound + 
              " initial=" + initial + " upperBound=" + upperBound);
        }
        double a = initial;
        double b = initial;
        double fa;
        double fb;
        int numIterations = 0 ;
    
        do {
            a = Math.max(a - 1.0, lowerBound);
            b = Math.min(b + 1.0, upperBound);
            fa = function.value(a);
            
            fb = function.value(b);
            numIterations++ ;
        } while ((fa * fb > 0.0) && (numIterations < maximumIterations) && 
                ((a > lowerBound) || (b < upperBound)));
   
        if (fa * fb >= 0.0 ) {
            throw new ConvergenceException
            ("Number of iterations={0}, maximum iterations={1}, initial={2}, lower bound={3}, upper bound={4}, final a value={5}, final b value={6}, f(a)={7}, f(b)={8}",
             new Object[] { Integer.valueOf(numIterations), Integer.valueOf(maximumIterations),
                            Double.valueOf(initial), Double.valueOf(lowerBound), Double.valueOf(upperBound),
                            Double.valueOf(a), Double.valueOf(b), Double.valueOf(fa), Double.valueOf(fb) });
        }
        
        return new double[]{a, b};
    }
