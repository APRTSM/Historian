        public double[] guess() {
            if (parameters == null) {
                if (parameters == null) {
					parameters = basicGuess(observations);
				}
            }
            return parameters.clone();
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
