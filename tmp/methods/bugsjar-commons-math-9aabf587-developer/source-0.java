    public UniformRealDistribution(RandomGenerator rng,
                                   double lower,
                                   double upper,
                                   double inverseCumAccuracy)
        throws NumberIsTooLargeException {
        super(rng);
        if (lower >= upper) {
            throw new NumberIsTooLargeException(
                            LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                            lower, upper, false);
        }

        this.lower = lower;
        this.upper = upper;
        solverAbsoluteAccuracy = inverseCumAccuracy;
    }
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }
    public UniformRealDistribution(double lower, double upper)
        throws NumberIsTooLargeException {
        this(lower, upper, DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
    }
    public UniformRealDistribution(double lower, double upper, double inverseCumAccuracy)
        throws NumberIsTooLargeException {
        this(new  Well19937c(), lower, upper, inverseCumAccuracy);
    }
