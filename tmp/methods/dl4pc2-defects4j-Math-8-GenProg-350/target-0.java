    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        final double u2 = random.nextDouble();

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        double s = Double.NaN;

        return out;

    }
