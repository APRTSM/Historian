    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        final List<Pair<T, Double>> samples = new ArrayList<Pair<T, Double>>(
				probabilities.length);

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        if (sampleSize <= 0) {
			throw new NotStrictlyPositiveException(
					LocalizedFormats.NUMBER_OF_SAMPLES, sampleSize);
		}

        return out;

    }
