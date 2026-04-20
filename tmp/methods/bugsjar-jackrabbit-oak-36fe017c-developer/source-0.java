    public void dispose() {
        runBackgroundOperations();
        if (!isDisposed.getAndSet(true)) {
            synchronized (isDisposed) {
                isDisposed.notifyAll();
            }
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                // ignore
            }
            if (leaseUpdateThread != null) {
                try {
                    leaseUpdateThread.join();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            if (clusterNodeInfo != null) {
                clusterNodeInfo.dispose();
            }
            store.dispose();
            LOG.info("Disposed DocumentNodeStore with clusterNodeId: {}", clusterId);

            if (blobStore instanceof Closeable) {
                try {
                    ((Closeable) blobStore).close();
                } catch (IOException ex) {
                    LOG.debug("Error closing blob store " + blobStore, ex);
                }
            }
        }
        if (persistentCache != null) {
            persistentCache.close();
        }
    }
    public synchronized void runBackgroundOperations() {
        if (isDisposed.get()) {
            return;
        }
        if (simpleRevisionCounter != null) {
            // only when using timestamp
            return;
        }
        try {
            long start = clock.getTime();
            long time = start;
            // clean orphaned branches and collisions
            cleanOrphanedBranches();
            cleanCollisions();
            long cleanTime = clock.getTime() - time;
            time = clock.getTime();
            // split documents (does not create new revisions)
            backgroundSplit();
            long splitTime = clock.getTime() - time;
            time = clock.getTime();
            // write back pending updates to _lastRev
            backgroundWrite();
            long writeTime = clock.getTime() - time;
            time = clock.getTime();
            // pull in changes from other cluster nodes
            BackgroundReadStats readStats = backgroundRead(true);
            long readTime = clock.getTime() - time;
            String msg = "Background operations stats (clean:{}, split:{}, write:{}, read:{} {})";
            if (clock.getTime() - start > TimeUnit.SECONDS.toMillis(10)) {
                // log as info if it took more than 10 seconds
                LOG.info(msg, cleanTime, splitTime, writeTime, readTime, readStats);
            } else {
                LOG.debug(msg, cleanTime, splitTime, writeTime, readTime, readStats);
            }
        } catch (RuntimeException e) {
            if (isDisposed.get()) {
                return;
            }
            throw e;
        }
    }
    Commit newCommit(@Nullable Revision base,
                     @Nullable DocumentNodeStoreBranch branch) {
        if (base == null) {
            base = headRevision;
        }
        backgroundOperationLock.readLock().lock();
        boolean success = false;
        Commit c;
        try {
            c = new Commit(this, commitQueue.createRevision(), base, branch);
            success = true;
        } finally {
            if (!success) {
                backgroundOperationLock.readLock().unlock();
            }
        }
        return c;
    }
    MergeCommit newMergeCommit(@Nullable Revision base, int numBranchCommits) {
        if (base == null) {
            base = headRevision;
        }
        backgroundOperationLock.readLock().lock();
        boolean success = false;
        MergeCommit c;
        try {
            c = new MergeCommit(this, base, commitQueue.createRevisions(numBranchCommits));
            success = true;
        } finally {
            if (!success) {
                backgroundOperationLock.readLock().unlock();
            }
        }
        return c;
    }
