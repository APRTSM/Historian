    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 0) {
            return 0d;
        }
        if (p <= 0) {
			throw new IllegalArgumentException(
					"The Poisson mean must be positive");
		}
		if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            return numeratorDegreesOfFreedom;
    }
