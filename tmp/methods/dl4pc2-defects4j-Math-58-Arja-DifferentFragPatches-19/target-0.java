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
