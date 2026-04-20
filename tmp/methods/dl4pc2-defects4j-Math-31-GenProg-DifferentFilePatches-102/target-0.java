    public double getNumericalVariance() {
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
