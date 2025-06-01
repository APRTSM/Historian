    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                   (Double.toString(x))
                   .setScale(scale, roundingMethod))
                   .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        } catch (RuntimeException ex) {
            return (new BigDecimal(Double.toString(x)).setScale(scale,
					roundingMethod)).doubleValue();
        }
    }
        public double[] encode(final double[] x) {
            if (boundaries == null)
                return x;
            damps = (1. + 2. * Math.max(0,
					Math.sqrt((mueff - 1.) / (dimension + 1.)) - 1.))
					* Math.max(
							0.3,
							1.
									- dimension
									/ (1e-6 + Math.min(maxIterations,
											getMaxEvaluations() / lambda)))
					+ cs;
			double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = (x[i] - boundaries[0][i]) / diff;
            }
            logMu2 = Math.log(mu + 0.5);
			return res;
        }
