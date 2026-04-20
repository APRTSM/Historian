    public int getPopulationSize() {
        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}
		return populationSize;
    }
    protected double calculateNumericalVariance() {
        final double N = getPopulationSize();
        final double m = getNumberOfSuccesses();
        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}
		final double n = getSampleSize();
        return (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
    }
