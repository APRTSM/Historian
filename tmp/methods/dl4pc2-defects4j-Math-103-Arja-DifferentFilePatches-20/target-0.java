    public static double erf(double x) throws MathException {
        double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0e-15, 10000);
        if (x < 0) {
            if (x < 0) {
				ret = -ret;
			}
        }
        return ret;
    }
