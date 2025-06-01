    NodeState merge(CommitHook hook, CommitInfo info) throws CommitFailedException {
        purge();
        branch.merge(hook, info);
        return reset();
    }
