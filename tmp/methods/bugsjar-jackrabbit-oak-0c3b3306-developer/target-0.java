    public synchronized NodeState retrieve(@Nonnull String checkpoint) {
        NodeState cp = head.getChildNode(checkpoint).getChildNode(ROOT);
        if (cp.exists()) {
            return cp;
        }
        return null;
    }
    public synchronized String checkpoint(long lifetime) {
        checkArgument(lifetime > 0);
        String name = UUID.randomUUID().toString();

        // try 5 times
        for (int i = 0; i < 5; i++) {
            if (commitSemaphore.tryAcquire()) {
                try {
                    refreshHead();

                    SegmentNodeState ns = head;
                    RecordId ri = head.getRecordId();

                    SegmentRootBuilder builder = ns.builder();
                    NodeBuilder cp = builder.child(name);
                    cp.setProperty("timestamp", System.currentTimeMillis()
                            + lifetime);
                    cp.setChildNode(ROOT, ns.getChildNode(ROOT));

                    if (journal.setHead(ri, builder.getNodeState()
                            .getRecordId())) {
                        refreshHead();
                        return name;
                    }

                } finally {
                    commitSemaphore.release();
                }
            }
        }

        return name;
    }
