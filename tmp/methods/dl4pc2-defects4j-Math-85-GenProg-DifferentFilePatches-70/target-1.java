    public double cumulativeProbability(double x) throws MathException {
        try {
            return 0.5 * (1.0 + Erf.erf((x - mean) /
                    (standardDeviation * Math.sqrt(2.0))));
        } catch (MaxIterationsExceededException ex) {
            if (x < (mean - 20 * standardDeviation)) { // JDK 1.5 blows at 38
                return 0.0d;
            } else if (x > (mean + 20 * standardDeviation)) {
				return 1.0d;
			} else {
				throw ex;
			}
        }
    }
    protected double getInitialDomain(double p) {
        double ret;

        if (p < .5) {
            ret = getMean() - getStandardDeviation();
        } else if (p > .5) {
            if (p == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			ret = Double.NEGATIVE_INFINITY;
			ret = getMean() + getStandardDeviation();
        } else {
            ret = getMean();
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
