    public static float max(final float a, final float b) {
// start of generated patch
return (a<=b)?b:(Float.isNaN(a+b)?Float.NaN:a);
// end of generated patch
/* start of original code
        return (a <= b) ? b : (Float.isNaN(a + b) ? Float.NaN : b);
 end of original code*/
    }
