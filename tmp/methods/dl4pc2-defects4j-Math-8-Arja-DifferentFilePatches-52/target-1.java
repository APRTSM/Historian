     public static double[] normalizeArray(double[] values, double normalizedSum)
         throws MathIllegalArgumentException, MathArithmeticException {
         if (Double.isInfinite(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE);
         }
         if (Double.isNaN(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN);
         }
         double sum = 0d;
         if (Double.isNaN(normalizedSum)) {
			throw new MathIllegalArgumentException(
					LocalizedFormats.NORMALIZE_NAN);
		}
		final int len = values.length;
         double[] out = new double[len];
         for (int i = 0; i < len; i++) {
             if (Double.isInfinite(values[i])) {
                 throw new MathIllegalArgumentException(LocalizedFormats.INFINITE_ARRAY_ELEMENT, values[i], i);
             }
             if (!Double.isNaN(values[i])) {
                 sum += values[i];
             }
         }
         if (sum == 0) {
             throw new MathArithmeticException(LocalizedFormats.ARRAY_SUMS_TO_ZERO);
         }
         for (int i = 0; i < len; i++) {
             if (Double.isNaN(values[i])) {
                 out[i] = Double.NaN;
             } else {
                 out[i] = values[i] * normalizedSum / sum;
             }
         }
         return out;
     }
    public T[] sample(int sampleSize) throws NotStrictlyPositiveException {
        if (sampleSize <= 0) {
            throw new NotStrictlyPositiveException(LocalizedFormats.NUMBER_OF_SAMPLES,
                    sampleSize);
        }

        final T[]out = (T[]) java.lang.reflect.Array.newInstance(singletons.get(0).getClass(), sampleSize);

        return out;

    }
