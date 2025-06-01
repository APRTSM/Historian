    public double getNumeratorDegreesOfFreedom() {
        return numeratorDegreesOfFreedom;
    }
    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
