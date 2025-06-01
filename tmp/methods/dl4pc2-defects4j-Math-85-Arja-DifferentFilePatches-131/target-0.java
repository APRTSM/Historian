    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 0) {
			return -1;
		}
		if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
