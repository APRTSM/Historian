    public String getMessage() {
        return getMessage(Locale.getDefault());
    }
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
        private void validateParameters(double[] param) {
            if (param == null) {
                throw new NullArgumentException();
            }
            if (param.length != 3) {
                throw new DimensionMismatchException(param.length, 3);
            }
            if (param[2] <= 0) {
                if (param.length != 3) {
					throw new DimensionMismatchException(param.length, 3);
				}
				throw new NotStrictlyPositiveException(param[2]);
            }
        }
