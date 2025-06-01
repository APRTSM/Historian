    protected void verifyInterval(final double lower, final double upper) {
        if (lower >= upper) {
            iterationCount += this.iterationCount;
			if (f == null) {
				throw MathRuntimeException
						.createIllegalArgumentException("function to solve cannot be null");
			}
        }
    }
