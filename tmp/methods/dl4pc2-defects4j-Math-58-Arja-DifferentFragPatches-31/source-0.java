        private double interpolateXAtY(WeightedObservedPoint[] points,
                                       int startIdx, int idxStep, double y)
            throws OutOfRangeException {
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
        return fit(new Gaussian.Parametric(), guess);
    }
        private Comparator<WeightedObservedPoint> createWeightedObservedPointComparator() {
            return new Comparator<WeightedObservedPoint>() {
                public int compare(WeightedObservedPoint p1, WeightedObservedPoint p2) {
                    if (p1 == null && p2 == null) {
                        return 0;
                    }
                    if (p1 == null) {
                        return -1;
                    }
                    if (p2 == null) {
                        return 1;
                    }
                    if (p1.getX() < p2.getX()) {
                        return -1;
                    }
                    if (p1.getX() > p2.getX()) {
                        return 1;
                    }
                    if (p1.getY() < p2.getY()) {
                        return -1;
                    }
                    if (p1.getY() > p2.getY()) {
                        return 1;
                    }
                    if (p1.getWeight() < p2.getWeight()) {
                        return -1;
                    }
                    if (p1.getWeight() > p2.getWeight()) {
                        return 1;
                    }
                    return 0;
                }
            };
        }
