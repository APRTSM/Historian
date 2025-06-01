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
    public String getLocalizedMessage() {
        return getMessage(Locale.US);
    }
        public double value(final double[] point) {
            double value;
            if (boundaries != null && isRepairMode) {
                evaluations.incrementCount();
				double[] repaired = repair(point);
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(repaired)) +
                        penalty(point, repaired);
            } else
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(point));
            return isMinimize ? value : -value;
        }
        public double[] encode(final double[] x) {
            if (boundaries == null)
                return x;
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                if (lambda <= 0)
					lambda = 4 + (int) (3. * Math.log(dimension));
				double diff = boundaries[1][i] - boundaries[0][i];
                res[i] = (x[i] - boundaries[0][i]) / diff;
            }
            return res;
        }
