    protected double getDomainUpperBound(double p) {
        double n = getNumeratorDegreesOfFreedom();
		return Double.MAX_VALUE;
    }
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
