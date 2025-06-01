        private boolean isBetween(double value, double boundary1, double boundary2) {
            if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			return (value >= boundary1 && value <= boundary2) ||
                   (value >= boundary2 && value <= boundary1);
        }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
