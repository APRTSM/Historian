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
    public static void close(Closeable closeable, String name, Logger log) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (log == null) {
                    // then fallback to use the own Logger
                    log = LOG;
                }
                if (name != null) {
                    log.warn("Cannot close: " + name + ". Reason: " + e.getMessage(), e);
                } else {
                    log.warn("Cannot close. Reason: " + e.getMessage(), e);
                }
            }
        }
    }
