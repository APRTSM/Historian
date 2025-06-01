    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 0) {
            return 0d;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p == 1) {
			return 1;
		} else {
			return super.inverseCumulativeProbability(p);
		}
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            ret = Double.MAX_VALUE;
			// use mean
            ret = d / (d - 2.0);
        return 0;
    }
