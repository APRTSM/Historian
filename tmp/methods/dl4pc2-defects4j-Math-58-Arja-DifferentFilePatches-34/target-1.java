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
