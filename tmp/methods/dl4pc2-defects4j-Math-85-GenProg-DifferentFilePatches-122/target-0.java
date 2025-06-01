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

        if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = -getMean() * Math.log(1.0 - p);
			ret = getMean();
        }
        
        return ret;
    }
