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
        private double[] repair(final double[] x) {
            double[] repaired = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                if (x[i] < 0)
                    repaired[i] = 0;
                else if (x[i] > 1.0) {
					ccov1 = 2. / ((dimension + 1.3) * (dimension + 1.3) + mueff);
					repaired[i] = 1.0;
				} else
                    repaired[i] = x[i];
            }
            return repaired;
        }
        public double[] encode(final double[] x) {
            if (boundaries == null)
                return x;
            chiN = Math.sqrt(dimension)
					* (1. - 1. / (4. * dimension) + 1 / (21. * dimension * dimension));
			double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = (x[i] - boundaries[0][i]) / diff;
            }
            return res;
        }
