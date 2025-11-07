    public int getSupportUpperBound() {
        if (!numericalVarianceIsCalculated) {
numericalVariance = getNumberOfSuccesses();
numericalVarianceIsCalculated = true;
}

return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
