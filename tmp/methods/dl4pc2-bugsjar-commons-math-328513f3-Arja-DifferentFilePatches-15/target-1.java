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
            C = triu(C, 0).add(triu(C, 1).transpose());
			for (int i = 0; i < x.length; i++) {
                if (x[i] < 0)
                    repaired[i] = 0;
                else if (x[i] > 1.0)
                    repaired[i] = 1.0;
                else
                    repaired[i] = x[i];
            }
            return repaired;
        }
        public double[] decode(final double[] x) {
            if (boundaries == null)
                return x;
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                historySize = 10 + (int) (3. * 10. * dimension / lambda);
				double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = diff * x[i] + boundaries[0][i];
            }
            return res;
        }
