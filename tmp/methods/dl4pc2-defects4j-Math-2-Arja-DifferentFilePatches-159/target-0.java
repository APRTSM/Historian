    public int getSupportLowerBound() {
        if (sampleSize < 0) {
			throw new NotPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
					sampleSize);
		}
		return FastMath.max(0,
                            getSampleSize() + getNumberOfSuccesses() - getPopulationSize());
    }
