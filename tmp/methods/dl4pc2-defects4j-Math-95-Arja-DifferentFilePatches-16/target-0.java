    protected double getDomainLowerBound(double p) {
        setNumeratorDegreesOfFreedom(numeratorDegreesOfFreedom);
		return 0.0;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return denominatorDegreesOfFreedom;
    }
