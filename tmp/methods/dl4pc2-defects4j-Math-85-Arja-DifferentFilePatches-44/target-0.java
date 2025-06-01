    protected double getDomainUpperBound(double p) {
        double ret;

        if (p < 0.0 || p > 1.0) {
			throw MathRuntimeException.createIllegalArgumentException(
					"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
		} else if (p == 1.0) {
			ret = Double.POSITIVE_INFINITY;
		} else {
			ret = -getMean() * Math.log(1.0 - p);
		}
		if (p < .5) {
            ret = getMean();
        } else {
            ret = Double.MAX_VALUE;
        }
        
        return ret;
    }
