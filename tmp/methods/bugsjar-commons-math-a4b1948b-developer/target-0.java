    public static boolean equalsIncludingNaN(float x, float y) {
        return (Float.isNaN(x) && Float.isNaN(y)) || equals(x, y, 1);
    }
    public static boolean equals(float x, float y) {
        return equals(x, y, 1);
    }
    public static boolean equals(float x, float y, float eps) {
        return equals(x, y, 1) || FastMath.abs(y - x) <= eps;
    }
    public static boolean equalsIncludingNaN(float x, float y, int maxUlps) {
        return (Float.isNaN(x) && Float.isNaN(y)) || equals(x, y, maxUlps);
    }
    public static boolean equalsIncludingNaN(float[] x, float[] y) {
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
    public static boolean equals(float[] x, float[] y) {
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
    public static boolean equalsIncludingNaN(float x, float y, float eps) {
        return equalsIncludingNaN(x, y) || (FastMath.abs(y - x) <= eps);
    }
    public static boolean equals(float x, float y, int maxUlps) {
        // Check that "maxUlps" is non-negative and small enough so that
        // NaN won't compare as equal to anything (except another NaN).
        assert maxUlps > 0 && maxUlps < NAN_GAP;

        int xInt = Float.floatToIntBits(x);
        int yInt = Float.floatToIntBits(y);

        // Make lexicographically ordered as a two's-complement integer.
        if (xInt < 0) {
            xInt = SGN_MASK_FLOAT - xInt;
        }
        if (yInt < 0) {
            yInt = SGN_MASK_FLOAT - yInt;
        }

        final boolean isEqual = FastMath.abs(xInt - yInt) <= maxUlps;

        return isEqual && !Float.isNaN(x) && !Float.isNaN(y);
    }
