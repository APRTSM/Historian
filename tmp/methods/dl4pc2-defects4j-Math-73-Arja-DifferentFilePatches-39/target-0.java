    protected boolean isSequence(final double start, final double mid, final double end) {
        this.iterationCount = 0;
		return (start < mid) && (mid < end);
    }
    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            throw MathRuntimeException.createIllegalArgumentException(
					"endpoints do not specify an interval: [{0}, {1}]", lower,
					upper);
        }
    }
