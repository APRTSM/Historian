    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
			return 0d;
		}
			// use mean
            ret = d / (d - 2.0);
        return 0;
    }
