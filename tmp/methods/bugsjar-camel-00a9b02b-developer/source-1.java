        public void close() throws IOException {
            if (value instanceof Closeable) {
                IOHelper.close((Closeable) value, value.getClass().getName(), LOG);
            } else if (value instanceof Scanner) {
                // special for Scanner as it does not implement Closeable
                Scanner scanner = (Scanner) value;
                scanner.close();

                IOException ioException = scanner.ioException();
                if (ioException != null) {
                    throw ioException;
                }
            }
        }
    public void close() throws IOException {
        if (it instanceof Closeable) {
            IOHelper.close((Closeable) it);
        } else if (it instanceof Scanner) {
            // special for Scanner as it does not implement Closeable
            ((Scanner) it).close();
        }
        // close the buffer as well
        bos.close();
        // we are now closed
        closed = true;
    }
