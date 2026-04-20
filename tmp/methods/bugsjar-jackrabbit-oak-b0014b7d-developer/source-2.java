    public static int getGcGen(ByteBuffer data) {
        return data.getInt(GC_GEN_OFFSET);
    }
    public int getGcGen() {
        return getGcGen(data);
    }
    public void writeSegment(SegmentId id, byte[] data, int offset, int length) throws IOException {
        fileStoreLock.writeLock().lock();
        try {
            int generation = Segment.getGcGen(wrap(data, offset, length));
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
    private static void generateTarFile(LinkedHashMap<UUID, byte[]> entries,
            File file) throws IOException {
        log.info("Regenerating tar file {}", file);
        TarWriter writer = new TarWriter(file);
        for (Map.Entry<UUID, byte[]> entry : entries.entrySet()) {
            UUID uuid = entry.getKey();
            byte[] data = entry.getValue();
            int generation = getGcGen(wrap(data));
            writer.writeEntry(
                    uuid.getMostSignificantBits(),
                    uuid.getLeastSignificantBits(),
                    data, 0, data.length, generation);
        }
        writer.close();
    }
