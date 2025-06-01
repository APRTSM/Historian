    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        if (p <= 0) {
				throw new IllegalArgumentException(
						"The Poisson mean must be positive");
			}
		return 0;
    }
