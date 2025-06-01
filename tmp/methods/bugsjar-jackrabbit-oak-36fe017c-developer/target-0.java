    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            // only dispose once
            return;
        }
        // notify background threads waiting on isDisposed
        synchronized (isDisposed) {
            isDisposed.notifyAll();
        }
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            // ignore
        }

        // do a final round of background operations after
        // the background thread stopped
        internalRunBackgroundOperations();

        if (leaseUpdateThread != null) {
            try {
                leaseUpdateThread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        // now mark this cluster node as inactive by
        // disposing the clusterNodeInfo
        if (clusterNodeInfo != null) {
            clusterNodeInfo.dispose();
        }
        store.dispose();

        if (blobStore instanceof Closeable) {
            try {
                ((Closeable) blobStore).close();
            } catch (IOException ex) {
                LOG.debug("Error closing blob store " + blobStore, ex);
            }
        }
        if (persistentCache != null) {
            persistentCache.close();
        }
        LOG.info("Disposed DocumentNodeStore with clusterNodeId: {}", clusterId);
    }
    private void checkOpen() throws IllegalStateException {
        if (isDisposed.get()) {
            throw new IllegalStateException("This DocumentNodeStore is disposed");
        }
    }
    MergeCommit newMergeCommit(@Nullable Revision base, int numBranchCommits) {
        if (base == null) {
            base = headRevision;
        }
        backgroundOperationLock.readLock().lock();
        checkOpen();
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
    public void runBackgroundOperations() {
        if (isDisposed.get()) {
            return;
        }
        try {
            internalRunBackgroundOperations();
        } catch (RuntimeException e) {
            if (isDisposed.get()) {
                LOG.warn("Background operation failed: " + e.toString(), e);
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
        checkOpen();
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
    private synchronized void internalRunBackgroundOperations() {
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
    }
