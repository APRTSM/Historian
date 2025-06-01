    protected double getInitialDomain(double p) {
        double ret;
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"probability of success must be between 0.0 and 1.0, inclusive.");
		}
		double d = getDenominatorDegreesOfFreedom();
            return 0d;
    }
