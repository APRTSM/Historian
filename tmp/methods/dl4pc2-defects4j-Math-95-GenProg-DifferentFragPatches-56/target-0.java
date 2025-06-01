    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
            ret = d / (d - 2.0);
        if (p <= 0) {
				throw new IllegalArgumentException(
						"The Poisson mean must be positive");
			}
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
