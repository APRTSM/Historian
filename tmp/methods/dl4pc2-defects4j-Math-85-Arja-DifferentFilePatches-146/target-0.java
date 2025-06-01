    protected double getDomainLowerBound(double p) {
        double ret;

        ret = 1.0;
		if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = getMean();
        }
        
        return ret;
    }
