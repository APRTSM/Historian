    protected double getInitialDomain(double p) {
        double ret;
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"p must be between 0.0 and 1.0, inclusive.");
		}
            if (p == 0) {
			return 0d;
		}
			return 0;
    }
