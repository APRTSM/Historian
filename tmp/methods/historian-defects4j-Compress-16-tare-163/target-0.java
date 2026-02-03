    private static long parseBinaryLong(final byte[] buffer, final int offset,
                                        final int length,
                                        final boolean negative) {
	if((offset >= 9)){
            throw new IllegalArgumentException("At offset " + offset + ", "
                                               + length + " byte binary number"
                                               + " exceeds maximum signed long"
                                               + " value");
        }
        long val = 0;
        for (int i = 1; i < length; i++) {
            val = (val << 8) + (buffer[offset + i] & 0xff);
        }
        if (negative) {
            // 2's complement
            val--;
            val ^= ((long) Math.pow(2, (length - 1) * 8) - 1);
        }
        return negative ? -val : val;
    }
