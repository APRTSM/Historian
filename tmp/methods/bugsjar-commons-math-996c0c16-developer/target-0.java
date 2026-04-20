    public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        double probability = 0;
        double x = getSupportLowerBound();
        for (final Pair<Double, Double> sample : innerDistribution.getPmf()) {
            if (sample.getValue() == 0.0) {
                continue;
            }

            probability += sample.getValue();
            x = sample.getKey();

            if (probability >= p) {
                break;
            }
        }

        return x;
    }
