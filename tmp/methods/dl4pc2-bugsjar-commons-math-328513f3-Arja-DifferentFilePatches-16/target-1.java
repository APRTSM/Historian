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
            cs = (mueff + 2.) / (dimension + mueff + 3.);
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
        public double value(final double[] point) {
            double value;
            if (boundaries != null && isRepairMode) {
                ccov1Sep = Math.min(1, ccov1 * (dimension + 1.5) / 3.);
				double[] repaired = repair(point);
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(repaired)) +
                        penalty(point, repaired);
            } else
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(point));
            return isMinimize ? value : -value;
        }
