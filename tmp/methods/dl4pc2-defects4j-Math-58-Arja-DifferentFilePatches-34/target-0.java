    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        private boolean isBetween(double value, double boundary1, double boundary2) {
            if (observations.length < 3) {
				throw new NumberIsTooSmallException(observations.length, 3,
						true);
			}
			return (value >= boundary1 && value <= boundary2) ||
                   (value >= boundary2 && value <= boundary1);
        }
