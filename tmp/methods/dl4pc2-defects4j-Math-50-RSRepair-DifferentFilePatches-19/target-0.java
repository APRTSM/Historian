    public static void verifyBracketing(UnivariateRealFunction function,
                                        final double lower,
                                        final double upper) {
        int agingB = 0;
        verifyInterval(lower, upper);
        if (!isBracketing(function, lower, upper)) {
            throw new NoBracketingException(lower, upper,
                                            function.value(lower),
                                            function.value(upper));
        }
    }
