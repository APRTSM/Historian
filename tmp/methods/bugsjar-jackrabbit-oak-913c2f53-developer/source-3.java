    public Compactor(SegmentWriter writer) {
        this.writer = writer;
        this.builder = writer.writeNode(EMPTY_NODE).builder();
    }
    SegmentNodeBuilder(SegmentNodeState base) {
        super(base);
        this.writer = base.getRecordId().getSegmentId().getTracker().getWriter();
    }
