    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            resultComputed = true;
			throw MathRuntimeException.createIllegalArgumentException(
                    "endpoints do not specify an interval: [{0}, {1}]",
                    lower, upper);
        }
    }
