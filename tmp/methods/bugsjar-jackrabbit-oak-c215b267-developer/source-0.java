    public Compactor(SegmentWriter writer) {
        this.writer = writer;
        this.builder =
                new SegmentNodeBuilder(writer.writeNode(EMPTY_NODE), writer);
    }
