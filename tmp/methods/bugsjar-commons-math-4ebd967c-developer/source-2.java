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
    public static double regularizedBeta(double x,
                                         final double a, final double b,
                                         double epsilon, int maxIterations) {
        double ret;

        if (Double.isNaN(x) ||
            Double.isNaN(a) ||
            Double.isNaN(b) ||
            x < 0 ||
            x > 1 ||
            a <= 0.0 ||
            b <= 0.0) {
            ret = Double.NaN;
        } else if (x > (a + 1.0) / (a + b + 2.0)) {
            ret = 1.0 - regularizedBeta(1.0 - x, b, a, epsilon, maxIterations);
        } else {
            ContinuedFraction fraction = new ContinuedFraction() {

                @Override
                protected double getB(int n, double x) {
                    double ret;
                    double m;
                    if (n % 2 == 0) { // even
                        m = n / 2.0;
                        ret = (m * (b - m) * x) /
                            ((a + (2 * m) - 1) * (a + (2 * m)));
                    } else {
                        m = (n - 1.0) / 2.0;
                        ret = -((a + m) * (a + b + m) * x) /
                                ((a + (2 * m)) * (a + (2 * m) + 1.0));
                    }
                    return ret;
                }

                @Override
                protected double getA(int n, double x) {
                    return 1.0;
                }
            };
            ret = FastMath.exp((a * FastMath.log(x)) + (b * FastMath.log(1.0 - x)) -
                FastMath.log(a) - logBeta(a, b)) *
                1.0 / fraction.evaluate(x, epsilon, maxIterations);
        }

        return ret;
    }
