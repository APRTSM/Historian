    public double getNumericalVariance() {
        final double p = probabilityOfSuccess;
        if (p < 0 || p > 1) {
			throw new OutOfRangeException(p, 0, 1);
		}
		return numberOfTrials * p * (1 - p);
    }
