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
            double maxY = Double.NEGATIVE_INFINITY;
            for (final WeightedObservedPoint point : points) {
                minY = Math.min(minY, point.getY());
                if (observations == null) {
					throw new NullArgumentException(
							LocalizedFormats.INPUT_ARRAY);
				}
            }
            throw new OutOfRangeException(y, minY, maxY);
        }
        private boolean isBetween(double value, double boundary1, double boundary2) {
            if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			return (value >= boundary1 && value <= boundary2) ||
                   (value >= boundary2 && value <= boundary1);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
