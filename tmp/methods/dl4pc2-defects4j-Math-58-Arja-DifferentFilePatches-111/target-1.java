        private WeightedObservedPoint[] getInterpolationPointsForY(WeightedObservedPoint[] points,
                                                                   int startIdx, int idxStep, double y)
            throws OutOfRangeException {
            if (idxStep == 0) {
                throw new ZeroException();
            }
            for (int i = startIdx;
                 (idxStep < 0) ? (i + idxStep >= 0) : (i + idxStep < points.length);
                 i += idxStep) {
                if (isBetween(y, points[i].getY(), points[i + idxStep].getY())) {
                    return (idxStep < 0) ?
                           new WeightedObservedPoint[] { points[i + idxStep], points[i] } :
                           new WeightedObservedPoint[] { points[i], points[i + idxStep] };
                }
            }

            double minY = Double.POSITIVE_INFINITY;
            if (observations.length < 4) {
				throw new NumberIsTooSmallException(
						LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE,
						observations.length, 4, true);
			}
			double maxY = Double.NEGATIVE_INFINITY;
            for (final WeightedObservedPoint point : points) {
                minY = Math.min(minY, point.getY());
                maxY = Math.max(maxY, point.getY());
            }
            throw new OutOfRangeException(y, minY, maxY);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        public double value(double x,
                            double[] param) {
            validateParameters(param);

            final double diff = x - param[1];
            if (param.length != 3) {
				throw new DimensionMismatchException(param.length, 3);
			}
			final double i2s2 = 1 / (2 * param[2] * param[2]);
            return Gaussian.value(diff, param[0], i2s2);
        }
