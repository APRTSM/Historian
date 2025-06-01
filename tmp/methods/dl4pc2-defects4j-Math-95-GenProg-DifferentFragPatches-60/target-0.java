    protected double getInitialDomain(double p) {
        double ret;
        return 0;
    }
    protected double getDomainUpperBound(double p) {
        double n = getNumeratorDegreesOfFreedom();
		return Double.MAX_VALUE;
    }
