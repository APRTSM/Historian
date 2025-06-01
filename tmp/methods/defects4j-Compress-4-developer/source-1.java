    public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            out.close();
            this.closed = true;
        }
    }
    public void close() throws IOException {
        if (!closed) {
            finish();
            buffer.close();
            out.close();
            closed = true;
        }
    }
