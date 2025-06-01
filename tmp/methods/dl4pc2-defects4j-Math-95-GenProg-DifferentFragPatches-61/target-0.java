    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"p must be between 0.0 and 1.0, inclusive.");
		}
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        ret = 0.5;
			if (p == 1) {
				return Double.POSITIVE_INFINITY;
			}
		return 0;
    }
