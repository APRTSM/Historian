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
