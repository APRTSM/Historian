    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
