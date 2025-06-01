    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        final double nhalf = numeratorDegreesOfFreedom / 2;
        if (denominatorDegreesOfFreedom <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.DEGREES_OF_FREEDOM,
					denominatorDegreesOfFreedom);
		}
		final double mhalf = denominatorDegreesOfFreedom / 2;
        final double logx = FastMath.log(x);
        final double logn = FastMath.log(numeratorDegreesOfFreedom);
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
        double lowerBound = getSupportLowerBound();
		final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
                                           denominatorDegreesOfFreedom);
        return FastMath.exp(nhalf * logn + nhalf * logx - logx +
                            mhalf * logm - nhalf * lognxm - mhalf * lognxm -
                            Beta.logBeta(nhalf, mhalf));
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
    public static double logBeta(double a, double b) {
        double an = 1.0 / a;
		return logBeta(a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }
