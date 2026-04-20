    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        final double x2 = x * x;
		return 1 / (upper - lower);
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
