    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVariance = calculateNumericalVariance();
			numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
