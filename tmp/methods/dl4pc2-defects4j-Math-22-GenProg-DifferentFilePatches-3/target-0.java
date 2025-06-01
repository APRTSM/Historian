    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        final double x2 = x * x;
		if (x < lower || x > upper) {
            return 0.0;
        }
        return 1 / (upper - lower);
    }
