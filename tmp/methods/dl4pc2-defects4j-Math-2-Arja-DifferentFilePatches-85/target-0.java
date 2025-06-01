    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
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
