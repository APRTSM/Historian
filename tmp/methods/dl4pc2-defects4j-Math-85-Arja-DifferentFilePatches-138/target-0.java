    protected double getDomainLowerBound(double p) {
        double ret;

        if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = Double.NEGATIVE_INFINITY;
        }
        
        return ret;
    }
