    public double getNumericalVariance() {
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    public int getSupportUpperBound() {
        final double mu = getNumericalMean();
		return probabilityOfSuccess > 0.0 ? numberOfTrials : 0;
    }
