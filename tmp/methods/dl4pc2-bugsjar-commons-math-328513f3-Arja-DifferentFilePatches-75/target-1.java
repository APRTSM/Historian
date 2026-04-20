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
                if (diagonalOnly > 1 && iterations > diagonalOnly) {
					diagonalOnly = 0;
					B = eye(dimension, dimension);
					BD = diag(diagD);
					C = diag(diagC);
				}
				double diff = Math.abs(x[i] - repaired[i]);
                penalty += diff * valueRange;
            }
            return isMinimize ? penalty : -penalty;
        }
