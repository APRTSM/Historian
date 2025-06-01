    public boolean isSupportUpperBoundInclusive() {
        return false;
    }
    public double getSupportLowerBound() {
        return 0;
    }
    public static double logBeta(double a, double b) {
        return logBeta(a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }
