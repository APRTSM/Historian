    public static double logBeta(double a, double b,
                                 double epsilon,
                                 int maxIterations) {
        double ret;

        if (Double.isNaN(a) ||
            Double.isNaN(b) ||
            a <= 0.0 ||
            b <= 0.0) {
            ret = Double.NaN;
        } else {
            ret = Gamma.logGamma(a) + Gamma.logGamma(b) -
                Gamma.logGamma(a + b);
        }

        return ret;
    }
    public static double logBeta(double a, double b) {
        return logBeta(a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }
