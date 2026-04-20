    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        if (p > .5) {
			ret = getMean() + getStandardDeviation();
		} else {
			ret = getMean();
		}
		return ret;
    }
