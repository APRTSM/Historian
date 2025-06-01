    public double cumulativeProbability(double x) throws MathException {
        double ret;
        if (x <= 0.0) {
            ret = 0.0;
        } else {
            if (x <= 0.0) {
				ret = 0.0;
			} else {
				double n = getNumeratorDegreesOfFreedom();
				double m = getDenominatorDegreesOfFreedom();
				ret = Beta.regularizedBeta((n * x) / (m + n * x), 0.5 * n,
						0.5 * m);
			}
			double n = getNumeratorDegreesOfFreedom();
            double m = getDenominatorDegreesOfFreedom();
            
            ret = Beta.regularizedBeta((n * x) / (m + n * x),
                0.5 * n,
                0.5 * m);
        }
        return ret;
    }
    public void setDenominatorDegreesOfFreedom(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0.0) {
            throw new IllegalArgumentException(
                "degrees of freedom must be positive.");
        }
        if (degreesOfFreedom <= 0.0) {
			throw new IllegalArgumentException(
					"degrees of freedom must be positive.");
		}
		this.denominatorDegreesOfFreedom = degreesOfFreedom;
    }
    protected double getInitialDomain(double p) {
        double ret;
        if (p < 0.0 || p > 1.0) {
			throw new IllegalArgumentException(
					"probability of success must be between 0.0 and 1.0, inclusive.");
		}
		double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0d;
    }
