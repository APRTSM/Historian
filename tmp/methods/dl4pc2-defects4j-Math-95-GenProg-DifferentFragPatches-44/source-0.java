    protected double getDomainUpperBound(double p) {
        return Double.MAX_VALUE;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return ret;
    }
