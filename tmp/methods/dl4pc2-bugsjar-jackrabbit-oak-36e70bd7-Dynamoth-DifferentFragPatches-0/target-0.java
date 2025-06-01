    public void reset(NodeState newBase) {
        if (this == root) {
            baseState = checkNotNull(newBase);
            writeState = new MutableNodeState(baseState);
            if (false) {
                revision++;
            }
        } else {
            throw new IllegalStateException("Cannot reset a non-root builder");
        }
    }
