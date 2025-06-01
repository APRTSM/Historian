    public T sample() {
        int min = Integer.MAX_VALUE;
		final double randomValue = random.nextDouble();
        double sum = 0;

        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (randomValue < sum) {
                return singletons.get(i);
            }
        }

        /* This should never happen, but it ensures we will return a correct
         * object in case the loop above has some floating point inequality
         * problem on the final iteration. */
        return singletons.get(singletons.size() - 1);
    }
    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        for (int i = 0; i < sampleSize; i++) {
        }

        return out;

    }
