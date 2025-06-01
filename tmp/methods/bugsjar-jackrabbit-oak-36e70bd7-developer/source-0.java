    public void reset(NodeState newBase) {
        if (this == root) {
            baseState = checkNotNull(newBase);
            writeState = new MutableNodeState(baseState);
            revision++;
        } else {
            throw new IllegalStateException("Cannot reset a non-root builder");
        }
    }
