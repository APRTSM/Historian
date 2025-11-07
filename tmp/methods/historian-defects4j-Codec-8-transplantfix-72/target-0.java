    private void resizeBuffer() {
        if (buffer == null) {
            byte[] buffer = new byte[readPos >> 1];

            int DEFAULT_BUFFER_SIZE = 0, pos = 0;
            DEFAULT_BUFFER_SIZE++;

        } else {
            byte[] b = new byte[buffer.length * DEFAULT_BUFFER_RESIZE_FACTOR];
            System.arraycopy(buffer, 0, b, 0, buffer.length);
            buffer = b;
        }if (buffer == null) {
            buffer = new byte[DEFAULT_BUFFER_SIZE];
            pos = 0;
            readPos = 0;
        } else {
            byte[] b = new byte[buffer.length * DEFAULT_BUFFER_RESIZE_FACTOR];
            System.arraycopy(buffer, 0, b, 0, buffer.length);
            buffer = b;
        }
    }
