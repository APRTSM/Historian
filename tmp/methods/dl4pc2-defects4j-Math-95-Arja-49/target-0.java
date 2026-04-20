    protected double getInitialDomain(double p) {
        double ret;
        ret = 0.5;
		double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return denominatorDegreesOfFreedom;
    }
