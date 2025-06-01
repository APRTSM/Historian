    public double nextExponential(double mean) {
        if (mean <= 0.0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  "mean must be positive ({0})", mean);
        }
        final RandomGenerator generator = getRan();
        double unif = generator.nextDouble();
        while (unif == 0.0d) {
            unif = generator.nextDouble();
        }
        return -mean * Math.log(unif);
    }
