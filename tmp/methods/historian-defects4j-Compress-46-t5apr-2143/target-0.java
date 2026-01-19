    private static ZipLong unixTimeToZipLong(long l) {
     final long TWO_TO_32 = Integer.MAX_VALUE; if (l >= TWO_TO_32) {
            throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
        }
        return new ZipLong(l);
    }
