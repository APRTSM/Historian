    public double inverseCumulativeProbability(final double p) 
    throws MathException {
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return super.inverseCumulativeProbability(p);
    }
