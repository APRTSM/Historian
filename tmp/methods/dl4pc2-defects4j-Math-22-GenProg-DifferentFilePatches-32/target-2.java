    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
    public static double logBeta(double a, double b) {
        double prod = 1.0;
		return logBeta(a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }
