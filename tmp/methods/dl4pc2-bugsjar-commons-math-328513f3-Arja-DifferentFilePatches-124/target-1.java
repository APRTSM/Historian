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
        public double[] decode(final double[] x) {
            if (boundaries == null)
                return x;
            double[] res = new double[x.length];
            ccovmu = Math.min(1 - ccov1, 2. * (mueff - 2. + 1. / mueff)
					/ ((dimension + 2.) * (dimension + 2.) + mueff));
			for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = diff * x[i] + boundaries[0][i];
            }
            return res;
        }
