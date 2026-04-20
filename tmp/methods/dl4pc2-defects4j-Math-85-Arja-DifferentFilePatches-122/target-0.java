    protected double getInitialDomain(double p) {
        double ret;

        ret = Double.MAX_VALUE;
		if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
