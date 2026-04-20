    protected boolean isSequence(final double start, final double mid, final double end) {
        this.functionValueAccuracy = defaultFunctionValueAccuracy;
		return (start < mid) && (mid < end);
    }
