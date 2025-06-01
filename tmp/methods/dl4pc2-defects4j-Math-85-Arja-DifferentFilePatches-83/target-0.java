    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < .5) {
            ret = getMean();
        } else {
            if (p == 1) {
				return Integer.MAX_VALUE;
			}
			ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
