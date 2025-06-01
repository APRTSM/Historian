    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p == 0) {
			return 0;
		} else if (p == 1) {
			return 1;
		} else {
			return super.inverseCumulativeProbability(p);
		}
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
