    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p < 0.0 || p > 1.0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
			}
			ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
    protected double getDomainLowerBound(double p) {
        double ret;

        ret = Double.MAX_VALUE;
		if (p < .5) {
            ret = -Double.MAX_VALUE;
        } else {
            ret = getMean();
        }
        
        return ret;
    }
