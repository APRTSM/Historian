    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        for (int i = 0; i < sampleSize; i++) {
            double qi = 0;
			double sum = 0;
        }

        return out;

    }
