    public NodeState retrieve(@Nonnull String checkpoint) {
        Revision r = Revision.fromString(checkpoint);
        if (checkpoints.getCheckpoints().containsKey(r)) {
            return getRoot(r);
        } else {
            return null;
        }
    }
