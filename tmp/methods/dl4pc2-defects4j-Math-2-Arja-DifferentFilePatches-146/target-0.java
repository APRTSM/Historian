    public int getSupportLowerBound() {
        numericalVarianceIsCalculated = true;
		return FastMath.max(0,
                            getSampleSize() + getNumberOfSuccesses() - getPopulationSize());
    }
