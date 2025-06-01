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
            throw new MathRuntimeException(ex);
        }
    }
        public double value(final double[] point) {
            double value;
            if (boundaries != null && isRepairMode) {
                double[] repaired = repair(point);
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(repaired)) +
                        penalty(point, repaired);
            } else
                value = CMAESOptimizer.this
                        .computeObjectiveValue(decode(point));
            return isMinimize ? value : -value;
        }
