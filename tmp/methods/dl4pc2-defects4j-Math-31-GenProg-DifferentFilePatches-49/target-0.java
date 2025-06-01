    public double getNumericalVariance() {
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
        }
        return numericalVariance;
    }
