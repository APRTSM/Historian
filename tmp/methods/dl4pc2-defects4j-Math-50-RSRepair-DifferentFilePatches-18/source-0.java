    public static void verifyBracketing(UnivariateRealFunction function,
                                        final double lower,
                                        final double upper) {
        if (function == null) {
            throw new NullArgumentException(LocalizedFormats.FUNCTION);
        }
        verifyInterval(lower, upper);
        if (!isBracketing(function, lower, upper)) {
            throw new NoBracketingException(lower, upper,
                                            function.value(lower),
                                            function.value(upper));
        }
    }
