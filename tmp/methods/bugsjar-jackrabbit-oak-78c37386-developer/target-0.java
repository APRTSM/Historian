    public synchronized void flush() {
        if (length > 0) {
            int refcount = segment.getRefCount();

            int rootcount = roots.size();
            buffer[Segment.ROOT_COUNT_OFFSET] = (byte) (rootcount >> 8);
            buffer[Segment.ROOT_COUNT_OFFSET + 1] = (byte) rootcount;

            int blobrefcount = blobrefs.size();
            buffer[Segment.BLOBREF_COUNT_OFFSET] = (byte) (blobrefcount >> 8);
            buffer[Segment.BLOBREF_COUNT_OFFSET + 1] = (byte) blobrefcount;

            length = align(
                    refcount * 16 + rootcount * 3 + blobrefcount * 2 + length,
                    16);

            int pos = refcount * 16;
            if (pos + length <= buffer.length) {
                // the whole segment fits to the space *after* the referenced
                // segment identifiers we've already written, so we can safely
                // copy those bits ahead even if concurrent code is still
                // reading from that part of the buffer
                System.arraycopy(buffer, 0, buffer, buffer.length-length, pos);
                pos += buffer.length - length;
            } else {
                // this might leave some empty space between the header and
                // the record data, but this case only occurs when the
                // segment is >252kB in size and the maximum overhead is <<4kB,
                // which is acceptable
                length = buffer.length;
            }

            for (Map.Entry<RecordId, RecordType> entry : roots.entrySet()) {
                int offset = entry.getKey().getOffset();
                buffer[pos++] = (byte) entry.getValue().ordinal();
                buffer[pos++] = (byte) (offset >> (8 + Segment.RECORD_ALIGN_BITS));
                buffer[pos++] = (byte) (offset >> Segment.RECORD_ALIGN_BITS);
            }

            for (RecordId blobref : blobrefs) {
                int offset = blobref.getOffset();
                buffer[pos++] = (byte) (offset >> (8 + Segment.RECORD_ALIGN_BITS));
                buffer[pos++] = (byte) (offset >> Segment.RECORD_ALIGN_BITS);
            }

            SegmentId id = segment.getSegmentId();
            log.debug("Writing data segment {} ({} bytes)", id, length);
            store.writeSegment(id, buffer, buffer.length - length, length);

            // Keep this segment in memory as it's likely to be accessed soon
            ByteBuffer data;
            if (buffer.length - length > 4096) {
                data = ByteBuffer.allocate(length);
                data.put(buffer, buffer.length - length, length);
                data.rewind();
            } else {
                data = ByteBuffer.wrap(buffer, buffer.length - length, length);
            }
            tracker.setSegment(id, new Segment(tracker, id, data));

            buffer = createNewBuffer();
            roots.clear();
            blobrefs.clear();
            length = 0;
            position = buffer.length;
            segment = new Segment(tracker, buffer);
            segment.getSegmentId().setSegment(segment);
        }
    }
