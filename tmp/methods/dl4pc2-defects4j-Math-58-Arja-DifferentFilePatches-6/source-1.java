        private boolean isBetween(double value, double boundary1, double boundary2) {
            return (value >= boundary1 && value <= boundary2) ||
                   (value >= boundary2 && value <= boundary1);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit(new Gaussian.Parametric(), guess);
    }
        public double[] guess() {
            if (parameters == null) {
                parameters = basicGuess(observations);
            }
            return parameters.clone();
        }
        public double value(double x,
                            double[] param) {
            validateParameters(param);

            final double diff = x - param[1];
            final double i2s2 = 1 / (2 * param[2] * param[2]);
            return Gaussian.value(diff, param[0], i2s2);
        }
