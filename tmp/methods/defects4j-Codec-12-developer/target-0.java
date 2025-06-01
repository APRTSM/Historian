    public int available() throws IOException {
        // Note: the logic is similar to the InflaterInputStream:
        //       as long as we have not reached EOF, indicate that there is more
        //       data available. As we do not know for sure how much data is left,
        //       just return 1 as a safe guess.

        // use the EOF flag of the underlying codec instance
        if (baseNCodec.eof) {
            return 0;
        } else {
            return 1;
        }
    }
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("Negative skip length");
        }

        // skip in chunks of 512 bytes
        final byte[] b = new byte[512];
        final int max = (int) Math.min(n, Integer.MAX_VALUE);
        int total = 0;

        while (total < max) {
            int len = max - total;
            if (len > b.length) {
                len = b.length;
            }
            len = read(b, 0, len);
            if (len == EOF) {
                break;
            }
            total += len;
        }

        return total;
    }
