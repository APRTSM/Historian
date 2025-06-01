    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVarianceIsCalculated = true;
			numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
