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
