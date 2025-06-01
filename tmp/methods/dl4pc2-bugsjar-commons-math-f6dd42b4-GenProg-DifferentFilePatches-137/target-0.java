    protected void verifySequence(final double lower, final double initial, final double upper) {
        this.resultComputed = false;
		if (!isSequence(lower, initial, upper)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "invalid interval, initial value parameters:  lower={0}, initial={1}, upper={2}",
                    lower, initial, upper);
        }
    }
    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            if (lower >= upper) {
				throw MathRuntimeException.createIllegalArgumentException(
						"endpoints do not specify an interval: [{0}, {1}]",
						lower, upper);
			}
        }
    }
