    public static double gamma(final double x) {

        if ((x == FastMath.rint(x)) && (x <= 0.0)) {
            return Double.NaN;
        }

        final double ret;
        final double absX = FastMath.abs(x);
        if (x >= 1.0) {
			double prod = 1.0;
			double t = x;
			while (t > 2.5) {
				t -= 1.0;
				prod *= t;
			}
			ret = prod / (1.0 + invGamma1pm1(t - 1.0));
		} else {
			double prod = x;
			double t = x;
			while (t < -0.5) {
				t += 1.0;
				prod *= t;
			}
			ret = 1.0 / (prod * (1.0 + invGamma1pm1(t)));
		}
        return ret;
    }
