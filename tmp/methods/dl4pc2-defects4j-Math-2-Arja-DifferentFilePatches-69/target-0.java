    public int getSampleSize() {
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		return sampleSize;
    }
