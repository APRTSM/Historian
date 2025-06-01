    protected boolean isSequence(final double start, final double mid, final double end) {
        this.resultComputed = true;
		return (start < mid) && (mid < end);
    }
