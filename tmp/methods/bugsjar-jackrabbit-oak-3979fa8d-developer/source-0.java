        public void close() {
            file.blob = null;
            file.data = null;
        }
        public OakIndexInput clone() {
            return new OakIndexInput(this);
        }
        public void seek(long pos) throws IOException {
            file.seek(pos);
        }
