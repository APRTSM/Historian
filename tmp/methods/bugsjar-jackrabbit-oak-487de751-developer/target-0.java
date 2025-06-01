        public OakIndexFile(String name, NodeBuilder file) {
            this.name = name;
            this.file = file;
            this.blobSize = determineBlobSize(file);
            this.blob = new byte[blobSize];

            PropertyState property = file.getProperty(JCR_DATA);
            if (property != null && property.getType() == BINARIES) {
                this.data = newArrayList(property.getValue(BINARIES));
            } else {
                this.data = newArrayList();
            }

            this.length = (long)data.size() * blobSize;
            if (!data.isEmpty()) {
                Blob last = data.get(data.size() - 1);
                this.length -= blobSize - last.length();
            }
        }
        public void readBytes(byte[] b, int offset, int len)
                throws IOException {
            checkPositionIndexes(offset, offset + len, checkNotNull(b).length);

            if (len < 0 || position + len > length) {
                String msg = String.format("Invalid byte range request [%s] : position : %d, length : " +
                                "%d, len : %d", name, position, length, len);
                throw new IOException(msg);
            }

            int i = (int) (position / blobSize);
            int o = (int) (position % blobSize);
            while (len > 0) {
                loadBlob(i);

                int l = Math.min(len, blobSize - o);
                System.arraycopy(blob, o, b, offset, l);

                offset += l;
                len -= l;
                position += l;

                i++;
                o = 0;
            }
        }
