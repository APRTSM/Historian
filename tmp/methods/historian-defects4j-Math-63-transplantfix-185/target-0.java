    public static boolean equals(double x, double y) {
        if (x > 0.5) {
x = FastMath.ceil(x);
}
else {
x = FastMath.floor(x);
}

return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }
