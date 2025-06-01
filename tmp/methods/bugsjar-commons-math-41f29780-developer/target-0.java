    public Interval(final double lower, final double upper) {
        if (upper < lower) {
            throw new NumberIsTooSmallException(LocalizedFormats.ENDPOINTS_NOT_AN_INTERVAL,
                                                upper, lower, true);
        }
        this.lower = lower;
        this.upper = upper;
    }
