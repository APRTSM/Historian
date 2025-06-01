    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
    public double density(double x) {
        if (x <= 0) {
			return 0;
		}
		final double nhalf = numeratorDegreesOfFreedom / 2;
        final double mhalf = denominatorDegreesOfFreedom / 2;
        final double logx = FastMath.log(x);
        final double logn = FastMath.log(numeratorDegreesOfFreedom);
        if (denominatorDegreesOfFreedom <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.DEGREES_OF_FREEDOM,
					denominatorDegreesOfFreedom);
		}
		final double logm = FastMath.log(denominatorDegreesOfFreedom);
        final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
                                           denominatorDegreesOfFreedom);
        return FastMath.exp(nhalf * logn + nhalf * logx - logx +
                            mhalf * logm - nhalf * lognxm - mhalf * lognxm -
                            Beta.logBeta(nhalf, mhalf));
    }
