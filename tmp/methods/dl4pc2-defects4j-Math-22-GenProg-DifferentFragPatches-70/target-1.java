    public boolean isSupportUpperBoundInclusive() {
        double lowerBound = getSupportLowerBound();
		return true;
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
