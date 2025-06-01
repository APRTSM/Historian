    boolean wasCompacted(SegmentId id) {
        long msb = id.getMostSignificantBits();
        long lsb = id.getLeastSignificantBits();
        return findEntry(msb, lsb) != -1;
    }
    public Compactor(SegmentWriter writer) {
        this.writer = writer;
        this.builder =
                new SegmentNodeBuilder(writer.writeNode(EMPTY_NODE), writer);
    }
    protected SegmentTracker getTracker() {
        return segmentId.getTracker();
    }
    SegmentNodeBuilder(SegmentNodeState base, SegmentWriter writer) {
        super(base);
        this.writer = writer;
    }
    SegmentNodeBuilder(SegmentNodeState base) {
        this(base, base.getTracker().getWriter());
    }
