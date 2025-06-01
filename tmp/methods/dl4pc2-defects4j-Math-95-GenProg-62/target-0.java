    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            ret = Double.MAX_VALUE;
			// use mean
            ret = d / (d - 2.0);
        return 0;
    }
