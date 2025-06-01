    public static boolean isBracketing(UnivariateRealFunction function,
                                       final double lower,
                                       final double upper) {
        final double fLo = function.value(lower);
        final double fHi = function.value(upper);
        return (fLo >= 0 && fHi <= 0) || (fLo <= 0 && fHi >= 0);
    }
