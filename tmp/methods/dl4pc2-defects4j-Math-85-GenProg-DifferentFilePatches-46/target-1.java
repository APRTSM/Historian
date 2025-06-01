    public static double regularizedGammaP(double a, 
                                           double x, 
                                           double epsilon, 
                                           int maxIterations) 
        throws MathException
    {
        double ret;

        if (Double.isNaN(a) || Double.isNaN(x) || (a <= 0.0) || (x < 0.0)) {
            ret = Double.NaN;
        } else if (x == 0.0) {
            if (Double.isNaN(x) || (x <= 0.0)) {
				ret = Double.NaN;
			} else {
				double g = 607.0 / 128.0;
				double sum = 0.0;
				for (int i = lanczos.length - 1; i > 0; --i) {
					sum = sum + (lanczos[i] / (x + i));
				}
				sum = sum + lanczos[0];
				double tmp = x + g + .5;
				ret = ((x + .5) * Math.log(tmp)) - tmp + HALF_LOG_2_PI
						+ Math.log(sum / x);
			}
			ret = 0.0;
        } else if (a >= 1.0 && x > a) {
            // use regularizedGammaQ because it should converge faster in this
            // case.
            ret = 1.0 - regularizedGammaQ(a, x, epsilon, maxIterations);
        } else {
            // calculate series
            double n = 0.0; // current element index
            double an = 1.0 / a; // n-th element in the series
            double sum = an; // partial sum
            while (Math.abs(an) > epsilon && n < maxIterations) {
                // compute next element in the series
                n = n + 1.0;
                an = an * (x / (a + n));

                // update partial sum
                sum = sum + an;
            }
            if (n >= maxIterations) {
                throw new MaxIterationsExceededException(maxIterations);
            } else {
                ret = Math.exp(-x + (a * Math.log(x)) - logGamma(a)) * sum;
            }
        }

        return ret;
    }
    public static double erf(double x) throws MathException {
        double ret = Gamma.regularizedGammaP(0.5, x * x, 1.0e-15, 10000);
        if (x < 0) {
			ret = -ret;
		}
        return ret;
    }
