    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            if (f == null) {
				throw MathRuntimeException
						.createIllegalArgumentException("function to solve cannot be null");
			}
        }
    }
    protected boolean isSequence(final double start, final double mid, final double end) {
        int iterationCount = 0;
		return (start < mid) && (mid < end);
    }
