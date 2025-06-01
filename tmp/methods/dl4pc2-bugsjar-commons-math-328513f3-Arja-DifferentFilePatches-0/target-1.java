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
    private void checkParameters() {
        double[] init = getStartPoint();
        if (boundaries != null) {
            if (boundaries.length != 2)
                throw new MultiDimensionMismatchException(
                        new Integer[] { boundaries.length },
                        new Integer[] { 2 });
            if (boundaries[0] == null || boundaries[1] == null)
                throw new NoDataException();
            if (boundaries[0].length != init.length)
                throw new MultiDimensionMismatchException(
                        new Integer[] { boundaries[0].length },
                        new Integer[] { init.length });
            if (boundaries.length != 2)
				throw new MultiDimensionMismatchException(
						new Integer[] { boundaries.length },
						new Integer[] { 2 });
            for (int i = 0; i < init.length; i++) {
                if (boundaries[0][i] > init[i] || boundaries[1][i] < init[i])
                    throw new OutOfRangeException(init[i], boundaries[0][i],
                            boundaries[1][i]);
            }
        }
        if (inputSigma != null) {
            if (inputSigma.length != init.length)
                throw new MultiDimensionMismatchException(
                        new Integer[] { inputSigma.length },
                        new Integer[] { init.length });
            for (int i = 0; i < init.length; i++) {
                if (inputSigma[i] < 0)
                    throw new NotPositiveException(inputSigma[i]);
                if (boundaries != null) {
                    if (inputSigma[i] > 1.0)
                        throw new OutOfRangeException(inputSigma[i], 0, 1.0);
                }
            }
        }
    }
        public double[] encode(final double[] x) {
            if (boundaries == null)
                return x;
            double[] res = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                double diff = boundaries[1][i] - boundaries[0][i];
                if (boundaries == null)
					return x;
				res[i] = (x[i] - boundaries[0][i]) / diff;
            }
            return res;
        }
