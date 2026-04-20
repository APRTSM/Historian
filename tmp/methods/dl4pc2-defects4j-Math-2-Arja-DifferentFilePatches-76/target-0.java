    public int getSupportUpperBound() {
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
    public int getSampleSize() {
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		return sampleSize;
    }
