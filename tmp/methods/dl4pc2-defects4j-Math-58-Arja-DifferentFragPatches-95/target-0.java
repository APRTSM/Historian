        private double interpolateXAtY(WeightedObservedPoint[] points,
                                       int startIdx, int idxStep, double y)
            throws OutOfRangeException {
            if (observations.length < 3) {
					throw new NumberIsTooSmallException(observations.length, 3,
							true);
				}
			if (idxStep == 0) {
                throw new ZeroException();
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
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
