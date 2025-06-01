    public Compactor(SegmentWriter writer) {
        this.writer = writer;
        this.builder = writer.writeNode(EMPTY_NODE).builder();
    }
