    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public boolean isSupportLowerBoundInclusive() {
        final double nhalf = numeratorDegreesOfFreedom / 2;
		return true;
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
