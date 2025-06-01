    public synchronized String checkpoint(long lifetime) {
        checkArgument(lifetime > 0);
        // TODO: Guard the checkpoint from garbage collection
        return head.getRecordId().toString();
    }
    public synchronized NodeState retrieve(@Nonnull String checkpoint) {
        // TODO: Verify validity of the checkpoint
        RecordId id = RecordId.fromString(checkNotNull(checkpoint));
        SegmentNodeState root =
                new SegmentNodeState(store.getWriter().getDummySegment(), id);
        return root.getChildNode(ROOT);
    }
