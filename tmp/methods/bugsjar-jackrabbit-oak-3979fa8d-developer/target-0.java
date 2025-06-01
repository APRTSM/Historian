        public void seek(long pos) throws IOException {
            checkNotClosed();
            file.seek(pos);
        }
        private void checkNotClosed() {
            if (file.blob == null && file.data == null) {
                throw new AlreadyClosedException("Already closed: " + this);
            }
        }
        public OakIndexInput clone() {
            // TODO : shouldn't we call super#clone ?
            OakIndexInput clonedIndexInput = new OakIndexInput(this);
            clonedIndexInput.isClone = true;
            if (clones != null) {
                clones.put(clonedIndexInput, Boolean.TRUE);
            }
            return clonedIndexInput;
        }
        public void readBytes(byte[] b, int o, int n) throws IOException {
            checkNotClosed();
            file.readBytes(b, o, n);
        }
        public OakIndexInput(String name, NodeBuilder file) {
            super(name);
            this.file = new OakIndexFile(name, file);
            clones = WeakIdentityMap.newConcurrentHashMap();
        }
        private OakIndexInput(OakIndexInput that) {
            super(that.toString());
            this.file = new OakIndexFile(that.file);
            clones = null;
        }
        public void close() {
            file.blob = null;
            file.data = null;

            if (clones != null) {
                for (Iterator<OakIndexInput> it = clones.keyIterator(); it.hasNext();) {
                    final OakIndexInput clone = it.next();
                    assert clone.isClone;
                    clone.close();
                }
            }
        }
        public byte readByte() throws IOException {
            checkNotClosed();
            byte[] b = new byte[1];
            readBytes(b, 0, 1);
            return b[0];
        }
        public long length() {
            checkNotClosed();
            return file.length;
        }
        public long getFilePointer() {
            checkNotClosed();
            return file.position;
        }
