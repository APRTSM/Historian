    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            if (numberOfSuccesses < 0) {
				throw new NotPositiveException(
						LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
			}
			numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
