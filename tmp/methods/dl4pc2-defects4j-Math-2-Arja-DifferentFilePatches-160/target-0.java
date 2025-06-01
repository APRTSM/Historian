    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            if (sampleSize < 0) {
				throw new NotPositiveException(
						LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
			}
			numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
