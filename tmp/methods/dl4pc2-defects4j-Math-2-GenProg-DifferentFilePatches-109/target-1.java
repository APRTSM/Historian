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
        if (lower >= upper) {
			throw new NumberIsTooLargeException(
					LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower,
					upper, false);
		}

        return solveInverseCumulativeProbability(p, lower, upper);
    }
    protected double calculateNumericalVariance() {
        final double N = getPopulationSize();
        final double m = getNumberOfSuccesses();
        if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		if (numberOfSuccesses < 0) {
			throw new NotPositiveException(
					LocalizedFormats.NUMBER_OF_SUCCESSES, numberOfSuccesses);
		}
		final double n = getSampleSize();
        return (n * m * (N - n) * (N - m)) / (N * N * (N - 1));
    }
    public double getNumericalVariance() {
        if (!numericalVarianceIsCalculated) {
            numericalVariance = calculateNumericalVariance();
        }
        return numericalVariance;
    }
