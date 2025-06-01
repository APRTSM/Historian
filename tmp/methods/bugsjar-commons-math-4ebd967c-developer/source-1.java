    public double getNumericalVariance() {
        final double s = shape;
        final double ss = s * s;
        return (FastMath.exp(ss) - 1) * FastMath.exp(2 * scale + ss);
    }
    public double inverseCumulativeProbability(double p) {
        double ret;
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0.0, 1.0);
        } else if (p == 0) {
            ret = 0.0;
        } else  if (p == 1) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = scale * FastMath.pow(-FastMath.log(1.0 - p), 1.0 / shape);
        }
        return ret;
    }
