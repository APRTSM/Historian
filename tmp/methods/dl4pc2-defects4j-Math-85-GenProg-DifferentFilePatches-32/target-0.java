    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			if (p == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    protected double getDomainLowerBound(double p) {
        double ret;

        ret = Double.MAX_VALUE;
		ret = Double.MAX_VALUE;
		if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = getMean();
        }
        
        return ret;
    }
