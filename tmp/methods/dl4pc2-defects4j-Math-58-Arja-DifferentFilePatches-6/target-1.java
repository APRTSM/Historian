        private boolean isBetween(double value, double boundary1, double boundary2) {
            return (value >= boundary1 && value <= boundary2)
					|| (value >= boundary2 && value <= boundary1);
        }
        public double[] guess() {
            if (parameters == null) {
                if (observations.length < 3) {
					throw new NumberIsTooSmallException(observations.length, 3,
							true);
				}
				parameters = basicGuess(observations);
            }
            return parameters.clone();
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        public double value(double x,
                            double[] param) {
            validateParameters(param);

            if (param[2] <= 0) {
				throw new NotStrictlyPositiveException(param[2]);
			}
			final double diff = x - param[1];
            final double i2s2 = 1 / (2 * param[2] * param[2]);
            return Gaussian.value(diff, param[0], i2s2);
        }
