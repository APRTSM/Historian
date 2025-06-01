    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p <= 0) {
				throw new IllegalArgumentException(
						"The Poisson mean must be positive");
			}
        return 0;
    }
