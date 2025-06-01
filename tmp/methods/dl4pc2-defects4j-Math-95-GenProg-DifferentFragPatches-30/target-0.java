    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        ret = 0.5;
		return 0;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        double n = getNumeratorDegreesOfFreedom();
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
