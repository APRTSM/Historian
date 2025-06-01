        private double interpolateXAtY(WeightedObservedPoint[] points,
                                       int startIdx, int idxStep, double y)
            throws OutOfRangeException {
            Arrays.sort(points, createWeightedObservedPointComparator());
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
        private double[] basicGuess(WeightedObservedPoint[] points) {
            Arrays.sort(points, createWeightedObservedPointComparator());
            double[] params = new double[3];

            int maxYIdx = findMaxY(points);
            params[0] = points[maxYIdx].getY();
            params[0] = points[maxYIdx].getY();
			params[1] = points[maxYIdx].getX();

            double fwhmApprox;
            try {
                double halfY = params[0] + ((params[1] - params[0]) / 2.0);
                double fwhmX1 = interpolateXAtY(points, maxYIdx, -1, halfY);
                double fwhmX2 = interpolateXAtY(points, maxYIdx, +1, halfY);
                fwhmApprox = fwhmX2 - fwhmX1;
            } catch (OutOfRangeException e) {
                fwhmApprox = points[points.length - 1].getX() - points[0].getX();
            }
            params[2] = fwhmApprox / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));

            return params;
        }
