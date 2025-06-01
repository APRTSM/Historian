    public String getMessage() {
        return getMessage(Locale.getDefault());
    }
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
        private Comparator<WeightedObservedPoint> createWeightedObservedPointComparator() {
            return new Comparator<WeightedObservedPoint>() {
				public int compare(WeightedObservedPoint p1,
						WeightedObservedPoint p2) {
					if (p1 == null && p2 == null) {
						return 0;
					}
					if (p1 == null) {
						return -1;
					}
					if (p2 == null) {
						return 1;
					}
					if (p1.getX() < p2.getX()) {
						return -1;
					}
					if (p1.getX() > p2.getX()) {
						return 1;
					}
					if (p1.getY() < p2.getY()) {
						return -1;
					}
					if (p1.getY() > p2.getY()) {
						return 1;
					}
					if (p1.getWeight() < p2.getWeight()) {
						return -1;
					}
					if (p1.getWeight() > p2.getWeight()) {
						return 1;
					}
					return 0;
				}
			};
        }
