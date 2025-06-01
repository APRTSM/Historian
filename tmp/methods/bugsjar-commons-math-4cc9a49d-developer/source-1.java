    void setMean(double p);
    public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        setMean(p);
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }
    public PoissonDistributionImpl(double p, double epsilon) {
        setMean(p);
        this.epsilon = epsilon;
    }
    private void setNormalAndMeanInternal(NormalDistribution z,
                                          double p) {
        if (p <= 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
        }
        mean = p;
        normal = z;
        normal.setMean(p);
        normal.setStandardDeviation(FastMath.sqrt(p));
    }
    public PoissonDistributionImpl(double p, int maxIterations) {
        setMean(p);
        this.maxIterations = maxIterations;
    }
    public PoissonDistributionImpl(double p) {
        this(p, new NormalDistributionImpl());
    }
    public PoissonDistributionImpl(double p, NormalDistribution z) {
        super();
        setNormalAndMeanInternal(z, p);
    }
    public void setNormal(NormalDistribution value) {
        setNormalAndMeanInternal(value, mean);
    }
    public void setMean(double p) {
        setNormalAndMeanInternal(normal, p);
    }
