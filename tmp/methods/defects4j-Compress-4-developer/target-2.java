    public void close() throws IOException {
        if (!this.closed) {
            out.close();
            this.closed = true;
        }
    }
    public void close() throws IOException {
        if (!closed) {
            buffer.close();
            out.close();
            closed = true;
        }
    }
    public void close() throws IOException {
        if (raf != null) {
            raf.close();
        }
        if (out != null) {
            out.close();
        }
    }
