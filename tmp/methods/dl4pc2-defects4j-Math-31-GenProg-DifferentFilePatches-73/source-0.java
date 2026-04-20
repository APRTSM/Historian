    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }
    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
