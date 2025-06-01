    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVarianceIsCalculated = true;
			numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    protected double calculateNumericalVariance() {
        if (sampleSize > populationSize) {
			throw new NumberIsTooLargeException(
					LocalizedFormats.SAMPLE_SIZE_LARGER_THAN_POPULATION_SIZE,
					sampleSize, populationSize, true);
		}
		final double N = getPopulationSize();
        final double m = getNumberOfSuccesses();
        final double n = getSampleSize();
        return (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
    }
