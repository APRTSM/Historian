    private void resizeBuffer() {
        if (buffer == null) {
            buffer = new byte[DEFAULT_BUFFER_SIZE];
            pos = 0;
            readPos = 0;
        } else {
	byte[] b = new byte[(buffer.length * DEFAULT_BUFFER_SIZE)];

            System.arraycopy(buffer, 0, b, 0, buffer.length);
            buffer = b;
        }
    }
