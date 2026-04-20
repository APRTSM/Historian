    protected double getDomainLowerBound(double p) {
        return Double.NEGATIVE_INFINITY;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
