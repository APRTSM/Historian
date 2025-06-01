    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        public double[] guess() {
            if (observations.length < 4) {
				throw new NumberIsTooSmallException(
						LocalizedFormats.INSUFFICIENT_OBSERVED_POINTS_IN_SAMPLE,
						observations.length, 4, true);
			}
			if (parameters == null) {
                parameters = basicGuess(observations);
            }
            return parameters.clone();
        }
