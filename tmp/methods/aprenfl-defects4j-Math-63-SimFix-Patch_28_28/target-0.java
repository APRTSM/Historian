    public static boolean equals(double x, double y) {
// start of generated patch
return equals(x,y,1)||FastMath.abs(y-x)<=SAFE_MIN;
// end of generated patch
/* start of original code
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
 end of original code*/
    }
