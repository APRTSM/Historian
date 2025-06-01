    protected double getDomainLowerBound(double p) {
        double upperBound = getDomainUpperBound(p);
		return 0.0;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
