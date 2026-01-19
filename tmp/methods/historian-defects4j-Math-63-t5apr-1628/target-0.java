    public static boolean equals(double x, double y) {
     return (x == y && Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }
