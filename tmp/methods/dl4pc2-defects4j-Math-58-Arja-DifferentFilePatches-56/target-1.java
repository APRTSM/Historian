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
