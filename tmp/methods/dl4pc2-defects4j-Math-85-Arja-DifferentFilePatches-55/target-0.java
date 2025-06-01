    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        if (p == 0) {
			return 0;
		} else if (p == 1) {
			return 1;
		} else {
			return super.inverseCumulativeProbability(p);
		}
    }
