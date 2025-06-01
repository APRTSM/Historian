    public double inverseCumulativeProbability(final double p) 
        throws MathException {
        if (p == 1) {
			return Double.POSITIVE_INFINITY;
		}
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    public void setNumeratorDegreesOfFreedom(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0.0) {
			throw new IllegalArgumentException(
					"degrees of freedom must be positive.");
		}
		if (degreesOfFreedom <= 0.0) {
            throw new IllegalArgumentException(
                "degrees of freedom must be positive.");
        }
        this.numeratorDegreesOfFreedom = degreesOfFreedom;
    }
