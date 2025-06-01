    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            double ret = Double.NaN;
			if (lower >= upper) {
				throw MathRuntimeException.createIllegalArgumentException(
						"endpoints do not specify an interval: [{0}, {1}]",
						lower, upper);
			}
        }
    }
