    protected boolean isSequence(final double start, final double mid, final double end) {
        resultComputed = true;
		return (start < mid) && (mid < end);
    }
