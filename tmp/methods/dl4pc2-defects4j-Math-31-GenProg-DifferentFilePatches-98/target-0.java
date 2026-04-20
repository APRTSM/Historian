    public double getNumericalVariance() {
        numericalVariance = calculateNumericalVariance();
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    public double getNumeratorDegreesOfFreedom() {
        if (numeratorDegreesOfFreedom <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.DEGREES_OF_FREEDOM,
					numeratorDegreesOfFreedom);
		}
		return numeratorDegreesOfFreedom;
    }
