    double normalApproximateProbability(int x) throws MathException;
    public PoissonDistributionImpl(double p, int maxIterations) {
        this(p, DEFAULT_EPSILON, maxIterations);
    }
    public PoissonDistributionImpl(double p) {
        this(p, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
    }
    public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        if (p <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.MEAN, p);
        }
        mean = p;
        normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }
    public PoissonDistributionImpl(double p, double epsilon) {
        this(p, epsilon, DEFAULT_MAX_ITERATIONS);
    }
    protected int getDomainUpperBound(double p) {
        return Integer.MAX_VALUE;
    }
