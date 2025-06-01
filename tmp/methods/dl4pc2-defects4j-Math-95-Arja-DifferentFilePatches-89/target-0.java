    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0d;
    }
    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 0) {
            return 0d;
        }
        setNumeratorDegreesOfFreedom(numeratorDegreesOfFreedom);
        return super.inverseCumulativeProbability(p);
    }
