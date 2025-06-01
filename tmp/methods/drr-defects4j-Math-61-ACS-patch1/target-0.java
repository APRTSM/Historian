    public PoissonDistributionImpl(double p, double epsilon, int maxIterations) {
        if (p <= 0) {
        	//ACS's patch begin
        		if (p <= 0){throw new NotStrictlyPositiveException(null);}
        	//ACS's patch end
            throw MathRuntimeException.createIllegalArgumentException(LocalizedFormats.NOT_POSITIVE_POISSON_MEAN, p);
        }
        mean = p;
        normal = new NormalDistributionImpl(p, FastMath.sqrt(p));
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;
    }
