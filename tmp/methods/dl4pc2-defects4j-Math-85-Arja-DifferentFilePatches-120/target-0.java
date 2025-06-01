    protected double getDomainUpperBound(double p) {
        double ret;

        ret = -getMean() * Math.log(1.0 - p);
		if (p < .5) {
            ret = getMean();
        } else {
            ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
