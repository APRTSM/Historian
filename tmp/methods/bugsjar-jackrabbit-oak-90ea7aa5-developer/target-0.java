    public NodeState retrieve(@Nonnull String checkpoint) {
        Revision r = Revision.fromString(checkpoint);
        SortedMap<Revision, Info> checkpoints = this.checkpoints.getCheckpoints();
        if (checkpoints != null && checkpoints.containsKey(r)) {
            return getRoot(r);
        } else {
            return null;
        }
    }
