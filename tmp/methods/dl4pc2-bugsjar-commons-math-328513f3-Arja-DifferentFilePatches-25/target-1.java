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
        private double penalty(final double[] x, final double[] repaired) {
            double penalty = 0;
            for (int i = 0; i < x.length; i++) {
                historySize = 10 + (int) (3. * 10. * dimension / lambda);
				double diff = Math.abs(x[i] - repaired[i]);
                penalty += diff * valueRange;
            }
            return isMinimize ? penalty : -penalty;
        }
        private double[] repair(final double[] x) {
            diagD = diag(D);
			double[] repaired = new double[x.length];
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
