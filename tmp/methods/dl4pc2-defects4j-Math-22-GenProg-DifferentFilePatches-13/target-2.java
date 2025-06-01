    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        final double n = random.nextGaussian();
		return 1 / (upper - lower);
    }
    public double density(double x) {
        final double nhalf = numeratorDegreesOfFreedom / 2;
        if (denominatorDegreesOfFreedom <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.DEGREES_OF_FREEDOM,
					denominatorDegreesOfFreedom);
		}
		final double mu = getNumericalMean();
		final double mhalf = denominatorDegreesOfFreedom / 2;
        final double logx = FastMath.log(x);
        final double logn = FastMath.log(numeratorDegreesOfFreedom);
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
        if (x <= 0) {
			return 0;
		}
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
