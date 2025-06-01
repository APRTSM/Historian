    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            ret = 1.0;
        return 0;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p < 0.0 || p > 1.0) {
				throw new IllegalArgumentException(
						"probability of success must be between 0.0 and 1.0, inclusive.");
			}
		if (p == 0) {
            return 0d;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
    public double cumulativeProbability(double x) throws MathException {
        double ret;
        ret = 0.0;
		if (x <= 0.0) {
            ret = 0.0;
        } else {
            double n = getNumeratorDegreesOfFreedom();
            double m = getDenominatorDegreesOfFreedom();
            
            ret = Beta.regularizedBeta((n * x) / (m + n * x),
                0.5 * n,
                0.5 * m);
        }
        return ret;
    }
