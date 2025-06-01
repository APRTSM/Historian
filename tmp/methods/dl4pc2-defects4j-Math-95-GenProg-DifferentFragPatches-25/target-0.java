    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
			return 0d;
		}
			// use mean
            ret = d / (d - 2.0);
        ret = 0.5;
			if (p == 1) {
				return Double.POSITIVE_INFINITY;
			}
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
