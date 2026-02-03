    private static long getEncodeLength(byte[] pArray, int chunkSize, byte[] chunkSeparator) {
        // base64 always encodes to multiples of 4.
        chunkSize = (chunkSize / 4) * 4;

        long len = (pArray.length * 4) / 3;
        long mod = len % 4;
        if (mod != 0) {
            len += 4 - mod;
        }
        if (chunkSize > 0) {
            boolean lenChunksPerfectly = len % chunkSize == 0;
            len += (len / chunkSize) * chunkSeparator.length;
            if (!lenChunksPerfectly) {
                len += chunkSeparator.length;
            }
        }
        return len;
    }
