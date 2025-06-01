    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            double lowerBound = getDomainLowerBound(p);
			ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
