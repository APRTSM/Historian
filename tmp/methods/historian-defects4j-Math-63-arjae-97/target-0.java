    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!equals(y[i], x[i], PB)) {
                return false;
            }
        }
        return true;
    }
