    private static CommitHook newCommitHook(final String name,
            final PropertyState state) throws CommitFailedException {
        return new CommitHook() {
            @Override
            @Nonnull
            public NodeState processCommit(NodeState before, NodeState after,
                    CommitInfo info) throws CommitFailedException {
                // check for concurrent updates by this async task
                PropertyState stateAfterRebase = before.getChildNode(ASYNC)
                        .getProperty(name);
                if (Objects.equal(state, stateAfterRebase)) {
                    return postAsyncRunNodeStatus(after.builder(), name)
                            .getNodeState();
                } else {
                    throw CONCURRENT_UPDATE;
                }
            }
        };
    }
