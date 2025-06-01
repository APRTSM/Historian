    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p < 0.0 || p > 1.0) {
			throw MathRuntimeException.createIllegalArgumentException(
					"{0} out of [{1}, {2}] range", p, 0.0, 1.0);
		}
		return super.inverseCumulativeProbability(p);
    }
