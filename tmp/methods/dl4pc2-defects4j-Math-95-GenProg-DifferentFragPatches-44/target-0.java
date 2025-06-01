    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
			return 0d;
		}
			ret = Double.POSITIVE_INFINITY;
        return 0;
    }
    protected double getDomainUpperBound(double p) {
        double n = getNumeratorDegreesOfFreedom();
		return Double.MAX_VALUE;
    }
