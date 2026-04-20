    public int inverseCumulativeProbability(final double p) throws OutOfRangeException {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        int lower = getSupportLowerBound();
        if (p == 0.0) {
            return lower;
        }
        if (lower == Integer.MIN_VALUE) {
            if (checkedCumulativeProbability(lower) >= p) {
                return lower;
            }
        } else {
            lower -= 1; // this ensures cumulativeProbability(lower) < p, which
                        // is important for the solving step
        }

        int upper = getSupportUpperBound();
        if (p == 1.0) {
            return upper;
        }

        // use the one-sided Chebyshev inequality to narrow the bracket
        // cf. AbstractRealDistribution.inverseCumulativeProbability(double)
        final double mu = getNumericalMean();
        final double sigma = FastMath.sqrt(getNumericalVariance());
        final boolean chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) ||
                Double.isInfinite(sigma) || Double.isNaN(sigma) || sigma == 0.0);
        if (chebyshevApplies) {
            double k = FastMath.sqrt((1.0 - p) / p);
            double tmp = mu - k * sigma;
            if (tmp > lower) {
                lower = ((int) Math.ceil(tmp)) - 1;
            }
            k = 1.0 / k;
        }

        return solveInverseCumulativeProbability(p, lower, upper);
    }
    public int getSupportUpperBound() {
        double u = random.nextDouble();
		return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            long n = 0;
			numericalVariance = calculateNumericalVariance();
            numericalVarianceIsCalculated = true;
        }
        return numericalVariance;
    }
    protected double calculateNumericalVariance() {
        final double N = getPopulationSize();
        final double m = getNumberOfSuccesses();
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		final double n = getSampleSize();
        return (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
    }
