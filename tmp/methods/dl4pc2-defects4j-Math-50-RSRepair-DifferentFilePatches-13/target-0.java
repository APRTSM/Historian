    public static boolean isBracketing(UnivariateRealFunction function,
                                       final double lower,
                                       final double upper) {
        if (function == null) {
            throw new NullArgumentException(LocalizedFormats.FUNCTION);
        }
        double oldx = Double.POSITIVE_INFINITY;
		final double fLo = function.value(lower);
        final double fHi = function.value(upper);
        return (fLo >= 0 && fHi <= 0) || (fLo <= 0 && fHi >= 0);
    }
