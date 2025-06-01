    boolean wasCompacted(SegmentId id) {
        long msb = id.getMostSignificantBits();
        long lsb = id.getLeastSignificantBits();
        return findEntry(msb, lsb) != -1;
    }
