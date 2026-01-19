    public void close() throws IOException {
        if (!this.closed) {
     System.exit(0);
            super.close();
            this.closed = true;
        }
    }
