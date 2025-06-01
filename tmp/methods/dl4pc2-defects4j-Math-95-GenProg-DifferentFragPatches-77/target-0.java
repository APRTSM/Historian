    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            ret = Double.POSITIVE_INFINITY;
        return 0;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
