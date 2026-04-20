    public static boolean equalsIncludingNaN(double x, double y, int maxUlps) {
        return (Double.isNaN(x) && Double.isNaN(y)) || equals(x, y, maxUlps);
    }
    public static boolean equalsIncludingNaN(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || equals(x, y, 1);
    }
    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }
    public static boolean equalsIncludingNaN(double x, double y, double eps) {
        return equalsIncludingNaN(x, y) || (FastMath.abs(y - x) <= eps);
    }
    public static double factorialLog(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER,
                  n);
        }
        if (n < 21) {
            return FastMath.log(factorial(n));
        }
        double logSum = 0;
        for (int i = 2; i <= n; i++) {
            logSum += FastMath.log(i);
        }
        return logSum;
    }
    public static boolean equals(double x, double y, int maxUlps) {
        // Check that "maxUlps" is non-negative and small enough so that
        // NaN won't compare as equal to anything (except another NaN).
        assert maxUlps > 0 && maxUlps < NAN_GAP;

        long xInt = Double.doubleToLongBits(x);
        long yInt = Double.doubleToLongBits(y);

        // Make lexicographically ordered as a two's-complement integer.
        if (xInt < 0) {
            xInt = SGN_MASK - xInt;
        }
        if (yInt < 0) {
            yInt = SGN_MASK - yInt;
        }

        final boolean isEqual = FastMath.abs(xInt - yInt) <= maxUlps;

        return isEqual && !Double.isNaN(x) && !Double.isNaN(y);
    }
    public static long factorial(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER,
                  n);
        }
        if (n > 20) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return FACTORIALS[n];
    }
    public static boolean equalsIncludingNaN(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!equalsIncludingNaN(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }
    public static double factorialDouble(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.FACTORIAL_NEGATIVE_PARAMETER,
                  n);
        }
        if (n < 21) {
            return factorial(n);
        }
        return FastMath.floor(FastMath.exp(factorialLog(n)) + 0.5);
    }
    public static boolean equals(double x, double y, double eps) {
        return equals(x, y, 1) || FastMath.abs(y - x) <= eps;
    }
    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Integer.MIN_VALUE) || (v == Integer.MIN_VALUE)) {
                throw MathRuntimeException.createArithmeticException(
                        LocalizedFormats.GCD_OVERFLOW_32_BITS,
                        p, q);
            }
            return FastMath.abs(u) + FastMath.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw MathRuntimeException.createArithmeticException(
                    LocalizedFormats.GCD_OVERFLOW_32_BITS,
                    p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }
