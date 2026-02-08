    public void close() throws IOException {
        if (!this.closed) {
finish();
            super.close();
            this.closed = true;
        }
    }
