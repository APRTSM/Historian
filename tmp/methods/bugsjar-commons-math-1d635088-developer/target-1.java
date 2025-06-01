    public void nextBytes(byte[] bytes) {
        int bytesOut = 0;
        while (bytesOut < bytes.length) {
            int randInt = nextInt();
            for (int i = 0; i < 3; i++) {
                if (i > 0) {
                    randInt >>= 8;
                }
            }
            if (bytesOut < bytes.length) {
                bytes[bytesOut++] = (byte) randInt;
                if (bytesOut == bytes.length) {
                    return;
                }
            }
        }
    }
    public void nextBytes(byte[] bytes) {
        int i = 0;
        final int iEnd = bytes.length - 3;
        while (i < iEnd) {
            final int random = next(32);
            bytes[i]     = (byte) (random & 0xff);
            bytes[i + 1] = (byte) ((random >>  8) & 0xff);
            bytes[i + 2] = (byte) ((random >> 16) & 0xff);
            bytes[i + 3] = (byte) ((random >> 24) & 0xff);
            i += 4;
        }
        if (i < bytes.length) {
            int random = next(32);
            while (i < bytes.length) {
                bytes[i++] = (byte) (random & 0xff);
                random >>= 8;
            }
        }
    }
