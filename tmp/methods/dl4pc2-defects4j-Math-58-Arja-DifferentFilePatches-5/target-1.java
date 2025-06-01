        private int findMaxY(WeightedObservedPoint[] points) {
            if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			int maxYIdx = 0;
            for (int i = 1; i < points.length; i++) {
                if (points[i].getY() > points[maxYIdx].getY()) {
                    maxYIdx = i;
                }
            }
            return maxYIdx;
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        public double value(double x,
                            double[] param) {
            validateParameters(param);

            final double diff = x - param[1];
            if (param[2] <= 0) {
				throw new NotStrictlyPositiveException(param[2]);
			}
			final double i2s2 = 1 / (2 * param[2] * param[2]);
            return Gaussian.value(diff, param[0], i2s2);
        }
