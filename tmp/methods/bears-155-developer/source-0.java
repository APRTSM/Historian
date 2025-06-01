    private Commit newBranchCommit(@Nonnull Revision base,
                                   @Nullable DocumentNodeStoreBranch branch) {
        checkArgument(checkNotNull(base).isBranch(),
                "base must be a branch revision: " + base);

        checkOpen();
        return new Commit(this, newRevision(), base, branch);
    }
    void done(final @Nonnull Commit c, boolean isBranch, final @Nullable CommitInfo info) {
        if (commitQueue.contains(c.getRevision())) {
            try {
                commitQueue.done(c.getRevision(), new CommitQueue.Callback() {
                    @Override
                    public void headOfQueue(@Nonnull Revision revision) {
                        // remember before revision
                        Revision before = getHeadRevision();
                        // apply changes to cache based on before revision
                        c.applyToCache(before, false);
                        // track modified paths
                        changes.modified(c.getModifiedPaths());
                        // update head revision
                        setHeadRevision(c.getRevision());
                        dispatcher.contentChanged(getRoot(), info);
                    }
                });
            } finally {
                backgroundOperationLock.readLock().unlock();
            }
        } else {
            // branch commit
            c.applyToCache(c.getBaseRevision(), isBranch);
        }
    }
    void canceled(Commit c) {
        if (commitQueue.contains(c.getRevision())) {
            try {
                commitQueue.canceled(c.getRevision());
            } finally {
                backgroundOperationLock.readLock().unlock();
            }
        }
    }
