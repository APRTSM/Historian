    static double logBinomialProbability(int x, int n, double p, double q) {
        double ret;
        if (x == 0) {
            if (0.1 <= x) {
                ret = -getDeviancePart(n, n * q) - n * p;
            } else {
                ret = n * FastMath.log(q);
            }
        } else if (x == n) {
            if (q < 0.1) {
                ret = -getDeviancePart(n, n * p) - n * q;
            } else {
                ret = n * FastMath.log(p);
            }
        } else {
            ret = getStirlingError(n) - getStirlingError(x) -
                  getStirlingError(n - x) - getDeviancePart(x, n * p) -
                  getDeviancePart(n - x, n * q);
            double f = (MathUtils.TWO_PI * x * (n - x)) / n;
            ret = -0.5 * FastMath.log(f) + ret;
        }
        return ret;
    }
