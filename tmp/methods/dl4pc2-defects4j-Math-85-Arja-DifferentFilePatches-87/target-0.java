    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p == 1.0) {
				ret = Double.POSITIVE_INFINITY;
			} else {
				ret = -getMean() * Math.log(1.0 - p);
			}
        } else {
            ret = getMean();
        }
        
        return ret;
    }
