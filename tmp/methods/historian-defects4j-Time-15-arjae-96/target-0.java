    public static long safeMultiply(long val1, int val2) {
        if (val1 == 0 || val2 == 0) {
			return 0;
        }
        long total = val1 * val2;
        if (total / val2 != val1) {
          throw new ArithmeticException("Multiplication overflows a long: " + val1 + " * " + val2);
        }
        if (total / val2 != val1 || val1 == Long.MIN_VALUE && val2 == -1 || val2 == Long.MIN_VALUE && val1 == -1) {
			throw new ArithmeticException("Multiplication overflows a long: " + val1 + " * " + val2);
		}
        return total;
    }
