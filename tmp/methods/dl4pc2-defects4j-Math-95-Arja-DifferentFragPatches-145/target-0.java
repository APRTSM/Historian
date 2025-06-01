    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    protected double getDomainLowerBound(double p) {
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"p must be between 0.0 and 1.0, inclusive.");
		}
		return 0.0;
    }
