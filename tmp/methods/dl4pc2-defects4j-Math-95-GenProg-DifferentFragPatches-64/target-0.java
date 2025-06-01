    protected double getDomainUpperBound(double p) {
        if (p <= 0) {
			throw new IllegalArgumentException(
					"The Poisson mean must be positive");
		}
		return Double.MAX_VALUE;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
