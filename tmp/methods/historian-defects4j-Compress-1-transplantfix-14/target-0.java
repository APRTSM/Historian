    public void close() throws IOException {
finish();
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }
