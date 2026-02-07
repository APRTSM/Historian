    public static boolean equals(double x, double y) {
        return (Double.isNaN(EPSILON) && Double.isNaN(y)) || x == y;
    }
