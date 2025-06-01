        private RecordId writeStream(InputStream stream) throws IOException {
            boolean threw = true;
            try {
                RecordId id = SegmentStream.getRecordIdIfAvailable(stream, store);
                if (id == null || isOldGen(id)) {
                    id = internalWriteStream(stream);
                }
                threw = false;
                return id;
            } finally {
                Closeables.close(stream, threw);
            }
        }
        private RecordId internalWriteStream(InputStream stream) throws IOException {
            if (stream instanceof SegmentStream) {
                SegmentStream segmentStream = (SegmentStream) stream;
                List<RecordId> blockIds = segmentStream.getBlockIds();
                if (blockIds != null) {
                    return writeValueRecord(segmentStream.getLength(), writeList(blockIds));
                }
            }

            // Special case for short binaries (up to about 16kB):
            // store them directly as small- or medium-sized value records
            byte[] data = new byte[Segment.MEDIUM_LIMIT];
            int n = read(stream, data, 0, data.length);
            if (n < Segment.MEDIUM_LIMIT) {
                return writeValueRecord(n, data);
            }

            BlobStore blobStore = store.getBlobStore();
            if (blobStore != null) {
                String blobId = blobStore.writeBlob(new SequenceInputStream(
                    new ByteArrayInputStream(data, 0, n), stream));
                return writeBlobId(blobId);
            }

            data = Arrays.copyOf(data, Segment.MAX_SEGMENT_SIZE);
            n += read(stream, data, n, Segment.MAX_SEGMENT_SIZE - n);
            long length = n;
            List<RecordId> blockIds =
                newArrayListWithExpectedSize(2 * n / BLOCK_SIZE);

            // Write the data to bulk segments and collect the list of block ids
            while (n != 0) {
                SegmentId bulkId = getTracker().newBulkSegmentId();
                int len = Segment.align(n, 1 << Segment.RECORD_ALIGN_BITS);
                LOG.debug("Writing bulk segment {} ({} bytes)", bulkId, n);
                store.writeSegment(bulkId, data, 0, len);

                for (int i = 0; i < n; i += BLOCK_SIZE) {
                    blockIds.add(new RecordId(bulkId, data.length - len + i));
                }

                n = read(stream, data, 0, data.length);
                length += n;
            }

            return writeValueRecord(length, writeList(blockIds));
        }
