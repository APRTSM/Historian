    public static void verifyBracketing(UnivariateRealFunction function,
                                        final double lower,
                                        final double upper) {
        final UnivariateRealSolver solver = new BrentSolver();
        verifyInterval(lower, upper);
        if (!isBracketing(function, lower, upper)) {
            throw new NoBracketingException(lower, upper,
                                            function.value(lower),
                                            function.value(upper));
        }
    }
