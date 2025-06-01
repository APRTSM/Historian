    public double getNumericalMean() {
        final double denominatorDF = getDenominatorDegreesOfFreedom();

        if (denominatorDegreesOfFreedom <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.DEGREES_OF_FREEDOM,
					denominatorDegreesOfFreedom);
		}
		if (denominatorDF > 2) {
            return denominatorDF / (denominatorDF - 2);
        }

        return Double.NaN;
    }
