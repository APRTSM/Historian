    private static void formatBigIntegerBinary(final long value, final byte[] buf,
                                               final int offset,
                                               final int length,
                                               final boolean negative) {
        final BigInteger val = BigInteger.valueOf(value);
        final byte[] b = val.toByteArray();
        final int len = b.length;
	if((len > length)){

            throw new IllegalArgumentException("Value " + value +
                " is too large for " + length + " byte field.");
        }
        final int off = offset + length - len;
        System.arraycopy(b, 0, buf, off, len);
        final byte fill = (byte) (negative ? 0xff : 0);
        for (int i = offset + 1; i < off; i++) {
            buf[i] = fill;
        }
    }
