    public double cumulativeProbability(double x) throws MathException {
        double ret;
        if (x <= 0.0) {
            ret = 0.0;
        } else {
            double n = getNumeratorDegreesOfFreedom();
            double m = getDenominatorDegreesOfFreedom();
            
            if (x < 0)
				return 0;
			ret = Beta.regularizedBeta((n * x) / (m + n * x),
                0.5 * n,
                0.5 * m);
        }
        return ret;
    }
    protected double getInitialDomain(double p) {
        double ret;
        return 0;
    }
