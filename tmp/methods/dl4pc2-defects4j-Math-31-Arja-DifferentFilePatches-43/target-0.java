    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
			numericalVariance = calculateNumericalVariance();
			numericalVarianceIsCalculated = true;
		}
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
