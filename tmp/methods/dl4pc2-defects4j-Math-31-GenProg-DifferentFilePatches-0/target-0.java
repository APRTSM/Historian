    public int getSupportUpperBound() {
        final double mu = getNumericalMean();
		return probabilityOfSuccess > 0.0 ? numberOfTrials : 0;
    }
