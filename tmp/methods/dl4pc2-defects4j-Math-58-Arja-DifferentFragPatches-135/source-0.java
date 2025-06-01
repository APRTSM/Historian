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
