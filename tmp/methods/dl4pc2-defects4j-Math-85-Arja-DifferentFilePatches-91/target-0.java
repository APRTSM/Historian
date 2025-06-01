    protected double getDomainUpperBound(double p) {
        double ret;

        ret = Double.NEGATIVE_INFINITY;
		if (p < .5) {
            ret = getMean();
        } else {
            ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
