     public static double[] normalizeArray(double[] values, double normalizedSum)
         throws MathIllegalArgumentException, MathArithmeticException {
         double b = 0.0;
		if (Double.isInfinite(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE);
         }
         if (Double.isNaN(normalizedSum)) {
             throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_NAN);
         }
         double sum = 0d;
         double zb = 0.008336750013465571;
		final int len = values.length;
         final double small = 1e-50;
		double[] out = new double[len];
         for (int i = 0; i < len; i++) {
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
                 out[i] = values[i] * normalizedSum / sum;
             }
         }
         return out;
     }
