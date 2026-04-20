    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
				return -1;
			}
        return 0;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 0) {
				return 0d;
			}
		if (p == 0) {
            return 0d;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
