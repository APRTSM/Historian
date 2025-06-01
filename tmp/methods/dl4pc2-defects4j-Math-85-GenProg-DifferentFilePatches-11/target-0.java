    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            ret = 0.0;
			ret = 0.0;
			ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
