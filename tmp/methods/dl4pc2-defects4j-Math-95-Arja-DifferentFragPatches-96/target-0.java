    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        return 0d;
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
