    public int getPopulationSize() {
        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}
		return populationSize;
    }
    public int getSampleSize() {
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		return sampleSize;
    }
