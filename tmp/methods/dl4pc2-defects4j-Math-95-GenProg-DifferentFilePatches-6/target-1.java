    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    public double cumulativeProbability(double x) throws MathException {
        double ret;
        if (x <= 0.0) {
            ret = 0.0;
        } else {
            double n = getNumeratorDegreesOfFreedom();
            double[] bracket = null;
			double m = getDenominatorDegreesOfFreedom();
            
            ret = Beta.regularizedBeta((n * x) / (m + n * x),
                0.5 * n,
                0.5 * m);
        }
        return ret;
    }
    public static double[] bracket(UnivariateRealFunction function, 
            double initial, double lowerBound, double upperBound) 
    throws ConvergenceException, FunctionEvaluationException {
        if (initial < lowerBound || initial > upperBound
				|| lowerBound >= upperBound) {
			throw new IllegalArgumentException(
					"Invalid endpoint parameters:  lowerBound=" + lowerBound
							+ " initial=" + initial + " upperBound="
							+ upperBound);
		}
		return bracket( function, initial, lowerBound, upperBound,
            Integer.MAX_VALUE ) ;
    }
