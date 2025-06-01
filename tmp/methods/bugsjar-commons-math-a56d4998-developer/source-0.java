    public static double[] bracket(final UnivariateFunction function, final double initial,
                                   final double lowerBound, final double upperBound,
                                   final double q, final double r, final int maximumIterations)
        throws NoBracketingException {

        if (function == null) {
            throw new NullArgumentException(LocalizedFormats.FUNCTION);
        }
        if (q <= 0)  {
            throw new NotStrictlyPositiveException(q);
        }
        if (maximumIterations <= 0)  {
            throw new NotStrictlyPositiveException(LocalizedFormats.INVALID_MAX_ITERATIONS, maximumIterations);
        }
        verifySequence(lowerBound, initial, upperBound);

        // initialize the recurrence
        double a     = initial;
        double b     = initial;
        double fa    = Double.NaN;
        double fb    = Double.NaN;
        double delta = 0;

        for (int numIterations = 0;
             (numIterations < maximumIterations) && (a > lowerBound || b > upperBound);
             ++numIterations) {

            final double previousA  = a;
            final double previousFa = fa;
            final double previousB  = b;
            final double previousFb = fb;

            delta = r * delta + q;
            a     = FastMath.max(initial - delta, lowerBound);
            b     = FastMath.min(initial + delta, upperBound);
            fa    = function.value(a);
            fb    = function.value(b);

            if (numIterations == 0) {
                // at first iteration, we don't have a previous interval
                // we simply compare both sides of the initial interval
                if (fa * fb <= 0) {
                    // the first interval already brackets a root
                    return new double[] { a, b };
                }
            } else {
                // we have a previous interval with constant sign and expand it,
                // we expect sign changes to occur at boundaries
                if (fa * previousFa <= 0) {
                    // sign change detected at near lower bound
                    return new double[] { a, previousA };
                } else if (fb * previousFb <= 0) {
                    // sign change detected at near upper bound
                    return new double[] { previousB, b };
                }
            }

        }

        // no bracketing found
        throw new NoBracketingException(a, b, fa, fb);

    }
