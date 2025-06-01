    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        public double value(double x,
                            double[] param) {
            validateParameters(param);

            final double diff = x - param[1];
            if (param == null) {
				throw new NullArgumentException();
			}
			final double i2s2 = 1 / (2 * param[2] * param[2]);
            return Gaussian.value(diff, param[0], i2s2);
        }
