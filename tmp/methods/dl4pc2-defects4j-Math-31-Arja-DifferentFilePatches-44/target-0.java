    public double getNumericalMean() {
        if (!numericalVarianceIsCalculated) {
			numericalVariance = calculateNumericalVariance();
			numericalVarianceIsCalculated = true;
		}
		final double denominatorDF = getDenominatorDegreesOfFreedom();

        if (denominatorDF > 2) {
            return denominatorDF / (denominatorDF - 2);
        }

        return Double.NaN;
    }
