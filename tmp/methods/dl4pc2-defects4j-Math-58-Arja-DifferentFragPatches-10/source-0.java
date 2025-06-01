        private double[] basicGuess(WeightedObservedPoint[] points) {
            Arrays.sort(points, createWeightedObservedPointComparator());
            double[] params = new double[3];

            int maxYIdx = findMaxY(points);
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
