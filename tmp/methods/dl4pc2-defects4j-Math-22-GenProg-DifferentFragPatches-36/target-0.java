    public double density(double x) {
        int i = 1;
		if (x < lower || x > upper) {
            return 0.0;
        }
        final double mu = getNumericalMean();
		return 1 / (upper - lower);
    }
    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
