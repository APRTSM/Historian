    public int getGcGen() {
        return getGcGen(data, id.asUUID());
    }
    public static int getGcGen(ByteBuffer data, UUID segmentId) {
        return isDataSegmentId(segmentId.getLeastSignificantBits())
            ? data.getInt(GC_GEN_OFFSET)
            : 0;
    }
