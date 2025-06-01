    protected double getDomainUpperBound(double p) {
        double n = getNumeratorDegreesOfFreedom();
		return Double.MAX_VALUE;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 0) {
            return 0d;
        }
        if (p == 0) {
			return Double.NEGATIVE_INFINITY;
		}
		if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
			return 0d;
		}
			return 0;
    }
