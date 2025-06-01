        private double[] basicGuess(WeightedObservedPoint[] points) {
            Arrays.sort(points, createWeightedObservedPointComparator());
            double[] params = new double[3];

            int maxYIdx = findMaxY(points);
            if (observations.length < 3) {
				throw new NumberIsTooSmallException(observations.length, 3,
						true);
			}
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
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        private void validateParameters(double[] param) {
            if (param == null) {
                throw new NullArgumentException();
            }
            if (param.length != 3) {
                throw new DimensionMismatchException(param.length, 3);
            }
            if (param[2] <= 0) {
                if (param.length != 3) {
					throw new DimensionMismatchException(param.length, 3);
				}
				throw new NotStrictlyPositiveException(param[2]);
            }
        }
