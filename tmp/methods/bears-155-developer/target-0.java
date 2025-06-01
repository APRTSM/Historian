    void canceled(Commit c) {
        if (commitQueue.contains(c.getRevision())) {
            try {
                commitQueue.canceled(c.getRevision());
            } finally {
                backgroundOperationLock.readLock().unlock();
            }
        } else {
            try {
                Branch b = branches.getBranch(c.getBaseRevision());
                if (b != null) {
                    b.removeCommit(c.getRevision().asBranchRevision());
                }
            } finally {
                if (isDisableBranches()) {
                    backgroundOperationLock.readLock().unlock();
                }
            }
        }
    }
    private Commit newBranchCommit(@Nonnull Revision base,
                                   @Nullable DocumentNodeStoreBranch branch) {
        checkArgument(checkNotNull(base).isBranch(),
                "base must be a branch revision: " + base);

        checkOpen();
        Commit c = new Commit(this, newRevision(), base, branch);
        if (isDisableBranches()) {
            // Regular branch commits do not need to acquire the background
            // operation lock because the head is not updated and no pending
            // lastRev updates are done on trunk. When branches are disabled,
            // a branch commit becomes a pseudo trunk commit and the lock
            // must be acquired.
            backgroundOperationLock.readLock().lock();
        }
        return c;
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
            try {
                c.applyToCache(c.getBaseRevision(), isBranch);
            } finally {
                if (isDisableBranches()) {
                    backgroundOperationLock.readLock().unlock();
                }
            }
        }
    }
