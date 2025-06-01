    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            return 0;
    }
    protected double getDomainLowerBound(double p) {
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"probability of success must be between 0.0 and 1.0, inclusive.");
		}
		return 0.0;
    }
