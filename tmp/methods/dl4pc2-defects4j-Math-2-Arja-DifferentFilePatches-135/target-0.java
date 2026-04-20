    public double getNumericalVariance() {
        if (sampleSize > populationSize) {
			throw new NumberIsTooLargeException(
					LocalizedFormats.SAMPLE_SIZE_LARGER_THAN_POPULATION_SIZE,
					sampleSize, populationSize, true);
		}
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    public int getPopulationSize() {
        if (sampleSize < 0) {
			throw new NotPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
					sampleSize);
		}
		return populationSize;
    }
