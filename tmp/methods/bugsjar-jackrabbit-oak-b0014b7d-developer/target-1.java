    public int getGcGen() {
        return getGcGen(data, id.asUUID());
    }
    public static int getGcGen(ByteBuffer data, UUID segmentId) {
        return isDataSegmentId(segmentId.getLeastSignificantBits())
            ? data.getInt(GC_GEN_OFFSET)
            : 0;
    }
    public void writeSegment(SegmentId id, byte[] data, int offset, int length) throws IOException {
        fileStoreLock.writeLock().lock();
        try {
            int generation = Segment.getGcGen(wrap(data, offset, length), id.asUUID());
            long size = writer.writeEntry(
                    id.getMostSignificantBits(),
                    id.getLeastSignificantBits(),
                    data, offset, length, generation);
            if (size >= maxFileSize) {
                newWriter();
            }
            approximateSize.addAndGet(TarWriter.BLOCK_SIZE + length + TarWriter.getPaddingSize(length));
        } finally {
            fileStoreLock.writeLock().unlock();
        }
    }
