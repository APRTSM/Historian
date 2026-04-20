    protected boolean isSequence(final double start, final double mid, final double end) {
        functionValueAccuracy = defaultFunctionValueAccuracy;
		return (start < mid) && (mid < end);
    }
