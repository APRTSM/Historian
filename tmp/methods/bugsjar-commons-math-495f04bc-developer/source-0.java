    private static double roundUnscaled(double unscaled, double sign,
        int roundingMethod) {
        switch (roundingMethod) {
        case BigDecimal.ROUND_CEILING :
            if (sign == -1) {
                unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            } else {
                unscaled = FastMath.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            }
            break;
        case BigDecimal.ROUND_DOWN :
            unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            break;
        case BigDecimal.ROUND_FLOOR :
            if (sign == -1) {
                unscaled = FastMath.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
            } else {
                unscaled = FastMath.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
            }
            break;
        case BigDecimal.ROUND_HALF_DOWN : {
            unscaled = nextAfter(unscaled, Double.NEGATIVE_INFINITY);
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
                unscaled = FastMath.ceil(unscaled);
            } else {
                unscaled = FastMath.floor(unscaled);
            }
            break;
        }
        case BigDecimal.ROUND_HALF_EVEN : {
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction > 0.5) {
                unscaled = FastMath.ceil(unscaled);
            } else if (fraction < 0.5) {
                unscaled = FastMath.floor(unscaled);
            } else {
                // The following equality test is intentional and needed for rounding purposes
                if (FastMath.floor(unscaled) / 2.0 == FastMath.floor(Math
                    .floor(unscaled) / 2.0)) { // even
                    unscaled = FastMath.floor(unscaled);
                } else { // odd
                    unscaled = FastMath.ceil(unscaled);
                }
            }
            break;
        }
        case BigDecimal.ROUND_HALF_UP : {
            unscaled = nextAfter(unscaled, Double.POSITIVE_INFINITY);
            double fraction = unscaled - FastMath.floor(unscaled);
            if (fraction >= 0.5) {
                unscaled = FastMath.ceil(unscaled);
            } else {
                unscaled = FastMath.floor(unscaled);
            }
            break;
        }
        case BigDecimal.ROUND_UNNECESSARY :
            if (unscaled != FastMath.floor(unscaled)) {
                throw new ArithmeticException("Inexact result from rounding");
            }
            break;
        case BigDecimal.ROUND_UP :
            unscaled = FastMath.ceil(nextAfter(unscaled,  Double.POSITIVE_INFINITY));
            break;
        default :
            throw MathRuntimeException.createIllegalArgumentException(
                  LocalizedFormats.INVALID_ROUNDING_METHOD,
                  roundingMethod,
                  "ROUND_CEILING",     BigDecimal.ROUND_CEILING,
                  "ROUND_DOWN",        BigDecimal.ROUND_DOWN,
                  "ROUND_FLOOR",       BigDecimal.ROUND_FLOOR,
                  "ROUND_HALF_DOWN",   BigDecimal.ROUND_HALF_DOWN,
                  "ROUND_HALF_EVEN",   BigDecimal.ROUND_HALF_EVEN,
                  "ROUND_HALF_UP",     BigDecimal.ROUND_HALF_UP,
                  "ROUND_UNNECESSARY", BigDecimal.ROUND_UNNECESSARY,
                  "ROUND_UP",          BigDecimal.ROUND_UP);
        }
        return unscaled;
    }
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }
    public static void checkOrder(double[] val, int dir, boolean strict) {
        if (dir > 0) {
            checkOrder(val, OrderDirection.INCREASING, strict);
        } else {
            checkOrder(val, OrderDirection.DECREASING, strict);
        }
    }
    public static double nextAfter(double d, double direction) {
        return FastMath.nextAfter(d, direction);
    }
