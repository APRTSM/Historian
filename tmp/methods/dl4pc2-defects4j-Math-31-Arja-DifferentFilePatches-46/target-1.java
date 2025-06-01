    public String getLocalizedMessage() {
        return context.getMessage();
    }
    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            return numericalVariance;
        }
        return numericalVariance;
    }
