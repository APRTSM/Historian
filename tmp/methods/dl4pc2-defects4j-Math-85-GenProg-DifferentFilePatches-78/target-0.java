    protected double getInitialDomain(double p) {
        double ret;

        if (p == 0) {
			return Double.NEGATIVE_INFINITY;
		}
		if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            ret = 0.0;
			ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
    protected double getDomainLowerBound(double p) {
        double ret;

        double value = 0;
		if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = getMean();
        }
        
        return ret;
    }
