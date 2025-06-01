    public double inverseCumulativeProbability(final double p)
            throws OutOfRangeException {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }
        return p * (upper - lower) + lower;
    }
    public UniformRealDistribution(RandomGenerator rng,
                                   double lower,
                                   double upper)
        throws NumberIsTooLargeException {
        super(rng);
        if (lower >= upper) {
            throw new NumberIsTooLargeException(
                            LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                            lower, upper, false);
        }

        this.lower = lower;
        this.upper = upper;
    }
    public UniformRealDistribution(double lower, double upper)
        throws NumberIsTooLargeException {
        this(new  Well19937c(), lower, upper);
    }
    public UniformRealDistribution(RandomGenerator rng,
                                   double lower,
                                   double upper,
                                   double inverseCumAccuracy){
        this(rng, lower, upper);
    }
    public UniformRealDistribution(double lower, double upper, double inverseCumAccuracy)
        throws NumberIsTooLargeException {
        this(new  Well19937c(), lower, upper);
    }
