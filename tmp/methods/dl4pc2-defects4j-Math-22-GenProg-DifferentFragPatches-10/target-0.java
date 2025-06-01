    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        final double n = random.nextGaussian();
		return 1 / (upper - lower);
    }
    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double getSupportUpperBound() {
        int j = 1;
		return upper;
    }
