    public int nextSecureInt(int lower, int upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        SecureRandom sec = getSecRan();
        return lower + (int) (sec.nextDouble() * (upper - lower + 1));
    }
    public long nextSecureLong(long lower, long upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        SecureRandom sec = getSecRan();
        return lower + (long) (sec.nextDouble() * (upper - lower + 1));
    }
    public double nextUniform(double lower, double upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        final RandomGenerator generator = getRan();

        // ensure nextDouble() isn't 0.0
        double u = generator.nextDouble();
        while (u <= 0.0) {
            u = generator.nextDouble();
        }

        return lower + u * (upper - lower);
    }
    public long nextLong(long lower, long upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        double r = getRan().nextDouble();
        return (long) ((r * upper) + ((1.0 - r) * lower) + r);
    }
    public int nextInt(int lower, int upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        double r = getRan().nextDouble();
        return (int) ((r * upper) + ((1.0 - r) * lower) + r);
    }
