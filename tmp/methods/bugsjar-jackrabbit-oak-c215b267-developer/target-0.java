    public SegmentNodeState compact(NodeState before, NodeState after) {
        SegmentNodeBuilder builder = new SegmentNodeBuilder(
                writer.writeNode(before), writer);
        after.compareAgainstBaseState(before, new CompactDiff(builder));
        return builder.getNodeState();
    }
