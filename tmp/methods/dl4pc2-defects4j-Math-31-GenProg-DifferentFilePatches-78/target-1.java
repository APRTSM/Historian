    public double getNumericalVariance() {
        numericalVariance = calculateNumericalVariance();
		if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    public String getMessage() {
        final String path = LocalizedFormats.class.getName().replaceAll("\\.",
				"/");
		return getMessage(Locale.US);
    }
