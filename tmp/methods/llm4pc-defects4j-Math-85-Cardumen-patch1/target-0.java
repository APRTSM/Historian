    protected double getDomainLowerBound(double p) {
        double ret;

        if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = ((standardDeviation) - (standardDeviation)) / ((mean) * (Math.sqrt(2.0)));
        }
        
        return ret;
    }
