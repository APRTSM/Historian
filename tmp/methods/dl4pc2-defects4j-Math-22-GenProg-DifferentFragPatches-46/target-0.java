    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        final double mu = getNumericalMean();
		return 1 / (upper - lower);
    }
