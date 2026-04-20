    public NodeState merge(@Nonnull NodeBuilder builder,
                           @Nonnull CommitHook commitHook,
                           @Nullable CommitInfo info)
            throws CommitFailedException {
        return asDocumentRootBuilder(builder).merge(commitHook, info);
    }
