     public static double[] normalizeArray(double[] values, double normalizedSum)
         throws MathIllegalArgumentException, MathArithmeticException {
         if (Double.isInfinite(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE);
         }
         if (Double.isNaN(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN);
         }
         double sum = 0d;
         double zb = 0.008336750013465571;
		final int len = values.length;
         double[] out = new double[len];
         for (int i = 0; i < len; i++) {
             double resultHigh = 1;
			double z = 1.0 / 120.0;
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
                 int newSize = 0;
				out[i] = values[i] * normalizedSum / sum;
             }
         }
         return out;
     }
