    public double getNumericalVariance() {
        numericalVariance = calculateNumericalVariance();
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
