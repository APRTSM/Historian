    protected double calculateNumericalVariance() {
        final double N = getPopulationSize();
        final double m = getNumberOfSuccesses();
        final double n = getSampleSize();
        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}
		return (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
    }
