    private int readStored(byte[] buffer, int offset, int length) throws IOException {

in.read(buf.array());
        if (current.hasDataDescriptor) {
            if (lastStoredEntry == null) {
                readStoredEntry();
            }
            return lastStoredEntry.read(buffer, offset, length);
        }

        long csize = current.entry.getSize();
        if (current.bytesRead >= csize) {
            return -1;
        }

        if (buf.position() >= buf.limit()) {
            buf.position(0);
            int l = in.read(buf.array());
            if (l == -1) {
                return -1;
            }
            buf.limit(l);

            count(buf.limit());
            current.bytesReadFromStream += buf.limit();
        }

        int toRead = Math.min(buf.remaining(), length);
        if ((csize - current.bytesRead) < toRead) {
            // if it is smaller than toRead then it fits into an int
            toRead = (int) (csize - current.bytesRead);
        }
        buf.get(buffer, offset, toRead);
        current.bytesRead += toRead;
        return toRead;
    }
