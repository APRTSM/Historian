    protected double getSolverAbsoluteAccuracy() {
        numericalVariance = calculateNumericalVariance();
		return solverAbsoluteAccuracy;
    }
