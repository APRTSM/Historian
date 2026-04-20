        private double interpolateXAtY(WeightedObservedPoint[] points,
                                       int startIdx, int idxStep, double y)
            throws OutOfRangeException {
            if (idxStep == 0) {
                throw new ZeroException();
            }
            if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			WeightedObservedPoint[] twoPoints = getInterpolationPointsForY(points, startIdx, idxStep, y);
            WeightedObservedPoint pointA = twoPoints[0];
            WeightedObservedPoint pointB = twoPoints[1];
            if (pointA.getY() == y) {
                return pointA.getX();
            }
            if (pointB.getY() == y) {
                return pointB.getX();
            }
            return pointA.getX() +
                   (((y - pointA.getY()) * (pointB.getX() - pointA.getX())) /
                    (pointB.getY() - pointA.getY()));
        }
        public double[] guess() {
            if (parameters == null) {
                if (parameters == null) {
					parameters = basicGuess(observations);
				}
				parameters = basicGuess(observations);
            }
            return parameters.clone();
        }
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
                if (observations.length < 4) {
					throw new NumberIsTooSmallException(
							LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE,
							observations.length, 4, true);
				}
                maxY = Math.max(maxY, point.getY());
            }
            throw new OutOfRangeException(y, minY, maxY);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
