    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p <= 0) {
				throw MathRuntimeException.createIllegalArgumentException(
						"the Poisson mean must be positive ({0})", p);
			}
			ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        return ret;
    }
