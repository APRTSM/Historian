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
