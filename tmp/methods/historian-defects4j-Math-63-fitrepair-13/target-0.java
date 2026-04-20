    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y))? false : equalsIncludingNaN(x, y, 1);
    }
