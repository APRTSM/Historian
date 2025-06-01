    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        int j = 1;
		final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        return out;

    }
