    public NodeState retrieve(@Nonnull String checkpoint) {
        return getRoot(Revision.fromString(checkpoint));
    }
