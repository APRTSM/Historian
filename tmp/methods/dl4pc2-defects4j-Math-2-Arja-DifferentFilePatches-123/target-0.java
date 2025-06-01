    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            if (numberOfSuccesses < 0) {
				throw new NotPositiveException(
						LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
			}
			numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
