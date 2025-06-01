        private Comparator<WeightedObservedPoint> createWeightedObservedPointComparator() {
            if (observations == null) {
				throw new NullArgumentException(LocalizedFormats.INPUT_ARRAY);
			}
			return new Comparator<WeightedObservedPoint>() {
                public int compare(WeightedObservedPoint p1, WeightedObservedPoint p2) {
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
    public double[] fit() {
        final double[] guess = (new ParameterGuesser(getObservations())).guess();
        return fit((new ParameterGuesser(getObservations())).guess());
    }
