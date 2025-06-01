    public int getSupportUpperBound() {
        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}
		return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
