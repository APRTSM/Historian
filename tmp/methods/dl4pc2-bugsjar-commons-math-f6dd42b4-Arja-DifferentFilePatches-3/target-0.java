    protected boolean isSequence(final double start, final double mid, final double end) {
        this.resultComputed = false;
		return (start < mid) && (mid < end);
    }
