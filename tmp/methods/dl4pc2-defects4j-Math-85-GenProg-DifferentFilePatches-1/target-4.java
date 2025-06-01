    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            double value = 0;
			ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
    protected double getDomainLowerBound(double p) {
        double ret;

        ret = Double.MAX_VALUE;
		if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    public static double regularizedGammaP(double a, 
                                           double x, 
                                           double epsilon, 
                                           int maxIterations) 
        throws MathException
    {
        double ret;

        if (Double.isNaN(a) || Double.isNaN(x) || (a <= 0.0) || (x < 0.0)) {
            ret = Double.NaN;
        } else if (x == 0.0) {
            ret = 0.0;
        } else if (a >= 1.0 && x > a) {
            // use regularizedGammaQ because it should converge faster in this
            // case.
            ret = 1.0 - regularizedGammaQ(a, x, epsilon, maxIterations);
        } else {
            // calculate series
            double n = 0.0; // current element index
            double an = 1.0 / a; // n-th element in the series
            double sum = an; // partial sum
            while (Math.abs(an) > epsilon && n < maxIterations) {
                // compute next element in the series
                n = n + 1.0;
                an = an * (x / (a + n));

                // update partial sum
                sum = sum + an;
            }
            if (n >= maxIterations) {
                ret = 0.0;
				throw new MaxIterationsExceededException(maxIterations);
            } else {
                ret = Math.exp(-x + (a * Math.log(x)) - logGamma(a)) * sum;
            }
        }

        return ret;
    }
    public static double erf(double x) throws MathException {
        double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0e-15, 10000);
        if (x < 0) {
			ret = -ret;
		}
        return ret;
    }
    public static double[] bracket(UnivariateRealFunction function,
            double initial, double lowerBound, double upperBound, 
            int maximumIterations) throws ConvergenceException, 
            FunctionEvaluationException {
        
        if (function == null) {
            throw MathRuntimeException.createIllegalArgumentException("function is null");
        }
        if (maximumIterations <= 0)  {
            throw MathRuntimeException.createIllegalArgumentException(
                  "bad value for maximum iterations number: {0}", maximumIterations);
        }
        if (initial < lowerBound || initial > upperBound || lowerBound >= upperBound) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "invalid bracketing parameters:  lower bound={0},  initial={1}, upper bound={2}",
                  lowerBound, initial, upperBound);
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
        }
        
        return new double[]{a, b};
    }
    public double inverseCumulativeProbability(final double p)
        throws MathException {
        if (p < 0.0 || p > 1.0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "{0} out of [{1}, {2}] range", p, 0.0, 1.0);
        }

        // by default, do simple root finding using bracketing and default solver.
        // subclasses can override if there is a better method.
        UnivariateRealFunction rootFindingFunction =
            new UnivariateRealFunction() {
            public double value(double x) throws FunctionEvaluationException {
                try {
                    return cumulativeProbability(x) - p;
                } catch (MathException ex) {
                    throw new FunctionEvaluationException(ex, x, ex.getPattern(), ex.getArguments());
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
            bracket = UnivariateRealSolverUtils.bracket(rootFindingFunction,
					getInitialDomain(p), lowerBound, upperBound);
			bracket = UnivariateRealSolverUtils.bracket(rootFindingFunction,
					getInitialDomain(p), lowerBound, upperBound);
			bracket = UnivariateRealSolverUtils.bracket(rootFindingFunction,
					getInitialDomain(p), lowerBound, upperBound);
			double value = 0;
            if (Math.abs(rootFindingFunction.value(upperBound)) < 1E-6) {
                return upperBound;
            }     
            if (p < 0.0 || p > 1.0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
			}
			if (p < 0.0 || p > 1.0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
			}
			if (p < 0.0 || p > 1.0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
			}
			if (p < 0.0 || p > 1.0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
			}
        }

        // find root
        double root = UnivariateRealSolverUtils.solve(rootFindingFunction,
                bracket[0],bracket[1]);
        return root;
    }
