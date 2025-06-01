    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
        }
        
        if (p <= 0) {
			throw MathRuntimeException.createIllegalArgumentException(
					"the Poisson mean must be positive ({0})", p);
		}
		return ret;
    }
    public static double erf(double x) throws MathException {
        double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0e-15, 10000);
        if (x < 0) {
			ret = -ret;
		}
        return ret;
    }
