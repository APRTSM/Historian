    public void reset(NodeState newBase) {
        if (this == root) {
            baseState = checkNotNull(newBase);
            writeState = new MutableNodeState(baseState);
            revision = 0;
        } else {
            throw new IllegalStateException("Cannot reset a non-root builder");
        }
    }
