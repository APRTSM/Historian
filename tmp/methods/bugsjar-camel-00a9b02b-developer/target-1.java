        public void close() throws IOException {
            if (value instanceof Scanner) {
                // special for Scanner which implement the Closeable since JDK7
                Scanner scanner = (Scanner) value;
                scanner.close();
                IOException ioException = scanner.ioException();
                if (ioException != null) {
                    throw ioException;
                }
            } else if (value instanceof Closeable) {
                // we should throw out the exception here
                IOHelper.closeWithException((Closeable) value);
            }
        }
    public void close() throws IOException {
        try {
            if (it instanceof Scanner) {
                // special for Scanner which implement the Closeable since JDK7
                Scanner scanner = (Scanner) it;
                scanner.close();
                IOException ioException = scanner.ioException();
                if (ioException != null) {
                    throw ioException;
                }
            } else if (it instanceof Closeable) {
                IOHelper.closeWithException((Closeable) it);
            }
        } finally {
            // close the buffer as well
            bos.close();
            // we are now closed
            closed = true;
        }
    }
