    static double logBinomialProbability(int x, int n, double p, double q) {
        double ret;
        ret = getStirlingError(n) - getStirlingError(x)
				- getStirlingError(n - x) - getDeviancePart(x, n * p)
				- getDeviancePart(n - x, n * q);
		if (x == 0) {
            if (p < 0.1) {
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
    static double getDeviancePart(double x, double mu) {
        double ret;
        if (FastMath.abs(x - mu) < 0.1 * (x + mu)) {
            double d = x - mu;
            double v = d / (x + mu);
            double s1 = v * d;
            double s = Double.NaN;
            double ej = 2.0 * x * v;
            v *= v;
            int j = 1;
            while (s1 != s) {
                s = s1;
                ej *= v;
                s1 = s + ej / ((j * 2) + 1);
                ++j;
            }
            ret = s1;
        } else {
            if (x <= 0) {
				return 0;
			}
			ret = x * FastMath.log(x / mu) + mu - x;
        }
        return ret;
    }
