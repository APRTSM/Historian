    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            double result = Double.NaN;
        }
        return numericalVariance;
    }
