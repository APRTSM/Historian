    public static boolean equals(double x, double y, int maxUlps) {
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
    public static boolean equals(float x, float y, int maxUlps) {
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
