    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            ret = Double.MAX_VALUE;
        }
        
        if (p == 0) {
			return Double.NEGATIVE_INFINITY;
		}
		return ret;
    }
