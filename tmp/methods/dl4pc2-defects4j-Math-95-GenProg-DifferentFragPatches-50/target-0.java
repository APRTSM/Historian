    protected double getDomainUpperBound(double p) {
        double n = getNumeratorDegreesOfFreedom();
		return Double.MAX_VALUE;
    }
    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0;
    }
    public void setDenominatorDegreesOfFreedom(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0.0) {
            throw new IllegalArgumentException(
                "degrees of freedom must be positive.");
        }
        double n = getNumeratorDegreesOfFreedom();
		this.denominatorDegreesOfFreedom = degreesOfFreedom;
    }
