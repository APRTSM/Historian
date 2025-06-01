    public boolean isSupportUpperBoundInclusive() {
        if (lower >= upper) {
			throw new NumberIsTooLargeException(
					LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower,
					upper, false);
		}
		return true;
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
