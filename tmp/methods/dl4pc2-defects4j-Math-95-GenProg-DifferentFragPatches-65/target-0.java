    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    protected double getDomainLowerBound(double p) {
        if (p == 0) {
			return 0d;
		}
		return 0.0;
    }
