    public int nextInt(int lower, int upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        double r = getRan().nextDouble();
        double scaled = r * upper + (1.0 - r) * lower + r;
        return (int)FastMath.floor(scaled);
    }
    public long nextSecureLong(long lower, long upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        SecureRandom sec = getSecRan();
        double r = sec.nextDouble();
        double scaled = r * upper + (1.0 - r) * lower + r;
        return (long)FastMath.floor(scaled);
    }
    public double nextUniform(double lower, double upper) {
        if (lower >= upper) {
            throw new MathIllegalArgumentException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper);
        }

        if (Double.isInfinite(lower) || Double.isInfinite(upper)) {
            throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_BOUND);
        }

        if (Double.isNaN(lower) || Double.isNaN(upper)) {
            throw new MathIllegalArgumentException(LocalizedFormats.NAN_NOT_ALLOWED);
        }

        final RandomGenerator generator = getRan();

        // ensure nextDouble() isn't 0.0
        double u = generator.nextDouble();
        while (u <= 0.0) {
            u = generator.nextDouble();
        }

        return u * upper + (1.0 - u) * lower;
    }
    public long nextLong(long lower, long upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        double r = getRan().nextDouble();
        double scaled = r * upper + (1.0 - r) * lower + r;
        return (long)FastMath.floor(scaled);
    }
    public int nextSecureInt(int lower, int upper) {
        if (lower >= upper) {
            throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND,
                                                lower, upper, false);
        }
        SecureRandom sec = getSecRan();
        double r = sec.nextDouble();
        double scaled = r * upper + (1.0 - r) * lower + r;
        return (int)FastMath.floor(scaled);
    }
