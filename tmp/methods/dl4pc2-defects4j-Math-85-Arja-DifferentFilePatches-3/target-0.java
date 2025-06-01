    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p == 1) {
			return Integer.MAX_VALUE;
		}
		return super.inverseCumulativeProbability(p);
    }
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
