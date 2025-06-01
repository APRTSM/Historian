    public double getNumericalVariance() {
        final double s = shape;
        final double ss = s * s;
        return (FastMath.expm1(ss)) * FastMath.exp(2 * scale + ss);
    }
