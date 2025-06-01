    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        double mean = 0;
		final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        return out;

    }
    public T sample() {
        final double randomValue = random.nextDouble();
        double v = 0;
		double sum = 0;

        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (randomValue < sum) {
                double qr = 0;
				return singletons.get(i);
            }
        }

        /* This should never happen, but it ensures we will return a correct
         * object in case the loop above has some floating point inequality
         * problem on the final iteration. */
        return singletons.get(singletons.size() - 1);
    }
