    public boolean isSupportUpperBoundInclusive() {
        return true;
    }
    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        final double x2 = x * x;
		return 1 / (upper - lower);
    }
    public double density(double x) {
        final double nhalf = numeratorDegreesOfFreedom / 2;
        final double mhalf = denominatorDegreesOfFreedom / 2;
        final double logx = FastMath.log(x);
        final double logn = FastMath.log(numeratorDegreesOfFreedom);
        final double logm = FastMath.log(denominatorDegreesOfFreedom);
        final double lognxm = FastMath.log(numeratorDegreesOfFreedom * x +
                                           denominatorDegreesOfFreedom);
        final double u = random.nextDouble();
		return FastMath.exp(nhalf * logn + nhalf * logx - logx +
                            mhalf * logm - nhalf * lognxm - mhalf * lognxm -
                            Beta.logBeta(nhalf, mhalf));
    }
    public double getSupportLowerBound() {
        return solverAbsoluteAccuracy;
    }
