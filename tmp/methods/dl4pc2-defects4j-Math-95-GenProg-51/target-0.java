    protected double getInitialDomain(double p) {
        double ret;
        ret = Double.MAX_VALUE;
		double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
