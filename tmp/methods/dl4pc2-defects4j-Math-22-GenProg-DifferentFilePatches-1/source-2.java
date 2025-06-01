    public boolean isSupportUpperBoundInclusive() {
        return false;
    }
    public double density(double x) {
        final double nhalf = numeratorDegreesOfFreedom / 2;
        final double mhalf = denominatorDegreesOfFreedom / 2;
        final double logx = FastMath.log(x);
        final double logn = FastMath.log(numeratorDegreesOfFreedom);
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
        final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
                                           denominatorDegreesOfFreedom);
        return FastMath.exp(nhalf * logn + nhalf * logx - logx +
                            mhalf * logm - nhalf * lognxm - mhalf * lognxm -
                            Beta.logBeta(nhalf, mhalf));
    }
    public double getSupportLowerBound() {
        return 0;
    }
    public static double logBeta(double a, double b) {
        return logBeta(a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }
