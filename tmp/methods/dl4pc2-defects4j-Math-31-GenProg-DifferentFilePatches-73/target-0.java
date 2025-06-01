    public double getNumericalVariance() {
        numericalVariance = calculateNumericalVariance();
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    protected double getSolverAbsoluteAccuracy() {
        double result = Double.NaN;
		return solverAbsoluteAccuracy;
    }
