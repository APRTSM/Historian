    public int getSupportLowerBound() {
        return FastMath.max(0, getSampleSize() + getNumberOfSuccesses()
				- getPopulationSize());
    }
