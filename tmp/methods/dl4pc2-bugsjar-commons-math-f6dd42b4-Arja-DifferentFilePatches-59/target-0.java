    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            this.functionValueAccuracy = defaultFunctionValueAccuracy;
			throw MathRuntimeException.createIllegalArgumentException(
                    "endpoints do not specify an interval: [{0}, {1}]",
                    lower, upper);
        }
    }
