    public int getSupportUpperBound() {
        numericalVariance = calculateNumericalVariance();
		return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
