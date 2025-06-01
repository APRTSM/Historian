    protected boolean isSequence(final double start, final double mid, final double end) {
        this.iterationCount = 0;
		return (start < mid) && (mid < end);
    }
