    public UniformIntegerDistribution(RandomGenerator rng,
                                      int lower,
                                      int upper)
        throws NumberIsTooLargeException {
        super(rng);

        if (lower > upper) {
            throw new NumberIsTooLargeException(
                            LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                            lower, upper, true);
        }
        this.lower = lower;
        this.upper = upper;
    }
