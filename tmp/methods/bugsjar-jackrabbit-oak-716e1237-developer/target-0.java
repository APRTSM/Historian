     private static void postAsyncRunStatsStatus(AsyncIndexStats stats) {
        stats.done(now());
    }
        void close() throws CommitFailedException {
            NodeBuilder builder = store.getRoot().builder();
            NodeBuilder async = builder.child(ASYNC);
            async.removeProperty(name + "-lease");
            mergeWithConcurrencyCheck(builder, async.getString(name), lease);
        }
        boolean isDirty() {
            return updates > 0;
        }
        public void indexUpdate() throws CommitFailedException {
            updates++;
            if (updates % 100 == 0) {
                long now = System.currentTimeMillis();
                if (now + ASYNC_TIMEOUT > lease) {
                    long newLease = now + 2 * ASYNC_TIMEOUT;
                    NodeBuilder builder = store.getRoot().builder();
                    builder.child(ASYNC).setProperty(name + "-lease", newLease);
                    mergeWithConcurrencyCheck(builder, checkpoint, lease);
                    lease = newLease;
                }
            }
        }
    private void mergeWithConcurrencyCheck(
            NodeBuilder builder, final String checkpoint, final long lease)
            throws CommitFailedException {
        CommitHook concurrentUpdateCheck = new CommitHook() {
            @Override @Nonnull
            public NodeState processCommit(
                    NodeState before, NodeState after, CommitInfo info)
                    throws CommitFailedException {
                // check for concurrent updates by this async task
                NodeState async = before.getChildNode(ASYNC);
                if (Objects.equal(checkpoint, async.getString(name))
                        && lease == async.getLong(name + "-lease")) {
                    return after;
                } else {
                    new Exception(checkpoint + " - " + async.getString(name)
                            + " / " + lease + " - " + async.getLong(name + "-lease")).printStackTrace();
                    throw CONCURRENT_UPDATE;
                }
            }
        };
        CompositeHook hooks = new CompositeHook(
                new ConflictHook(new AnnotatingConflictHandler()),
                new EditorHook(new ConflictValidatorProvider()),
                concurrentUpdateCheck);
        store.merge(builder, hooks, CommitInfo.EMPTY);
    }
    private void updateIndex(
            NodeState before, String beforeCheckpoint,
            NodeState after, String afterCheckpoint)
            throws CommitFailedException {
        // start collecting runtime statistics
        preAsyncRunStatsStats(indexStats);

        // create an update callback for tracking index updates
        // and maintaining the update lease
        AsyncUpdateCallback callback =
                new AsyncUpdateCallback(beforeCheckpoint);
        try {
            NodeBuilder builder = store.getRoot().builder();

            IndexUpdate indexUpdate =
                    new IndexUpdate(provider, name, after, builder, callback);
            CommitFailedException exception =
                    EditorDiff.process(indexUpdate, before, after);
            if (exception != null) {
                throw exception;
            }

            if (callback.isDirty() || before == MISSING_NODE) {
                builder.child(ASYNC).setProperty(name, afterCheckpoint);
                mergeWithConcurrencyCheck(
                        builder, beforeCheckpoint, callback.lease);

                if (switchOnSync) {
                    reindexedDefinitions.addAll(
                            indexUpdate.getReindexedDefinitions());
                } else {
                    postAsyncRunStatsStatus(indexStats);
                }
            } else if (switchOnSync) {
                log.debug("No changes detected after diff; will try to"
                        + " switch to synchronous updates on {}",
                        reindexedDefinitions);

                // no changes after diff, switch to sync on the async defs
                for (String path : reindexedDefinitions) {
                    NodeBuilder c = builder;
                    for (String p : elements(path)) {
                        c = c.getChildNode(p);
                    }
                    if (c.exists() && !c.getBoolean(REINDEX_PROPERTY_NAME)) {
                        c.removeProperty(ASYNC_PROPERTY_NAME);
                    }
                }

                mergeWithConcurrencyCheck(
                        builder, beforeCheckpoint, callback.lease);
                reindexedDefinitions.clear();
            }
        } finally {
            callback.close();
        }

        postAsyncRunStatsStatus(indexStats);
    }
        public AsyncUpdateCallback(String checkpoint)
                throws CommitFailedException {
            long now = System.currentTimeMillis();
            this.checkpoint = checkpoint;
            this.lease = now + 2 * ASYNC_TIMEOUT;

            String leaseName = name + "-lease";
            NodeState root = store.getRoot();
            long beforeLease = root.getChildNode(ASYNC).getLong(leaseName);
            if (beforeLease > now) {
                throw CONCURRENT_UPDATE;
            }

            NodeBuilder builder = root.builder();
            builder.child(ASYNC).setProperty(leaseName, lease);
            mergeWithConcurrencyCheck(builder, checkpoint, beforeLease);
        }
    public synchronized void run() {
        log.debug("Running background index task {}", name);

        NodeState root = store.getRoot();

        // check for concurrent updates
        NodeState async = root.getChildNode(ASYNC);
        if (async.getLong(name + "-lease") > System.currentTimeMillis()) {
            log.debug("Another copy of the {} index update is already running;"
                    + " skipping this update", name);
            return;
        }

        // find the last indexed state, and check if there are recent changes
        NodeState before;
        String beforeCheckpoint = async.getString(name);
        if (beforeCheckpoint != null) {
            NodeState state = store.retrieve(beforeCheckpoint);
            if (state == null) {
                log.warn("Failed to retrieve previously indexed checkpoint {};"
                        + " re-running the initial {} index update",
                        beforeCheckpoint, name);
                beforeCheckpoint = null;
                before = MISSING_NODE;
            } else if (noVisibleChanges(state, root)) {
                log.debug("No changes since last checkpoint;"
                        + " skipping the {} index update", name);
                return;
            } else {
                before = state;
            }
        } else {
            log.info("Initial {} index update", name);
            before = MISSING_NODE;
        }

        // there are some recent changes, so let's create a new checkpoint
        String afterCheckpoint = store.checkpoint(lifetime);
        NodeState after = store.retrieve(afterCheckpoint);
        if (after == null) {
            log.warn("Unable to retrieve newly created checkpoint {},"
                    + " skipping the {} index update", afterCheckpoint, name);
            return;
        }

        String checkpointToRelease = afterCheckpoint;
        try {
            updateIndex(before, beforeCheckpoint, after, afterCheckpoint);

            // the update succeeded, i.e. it no longer fails
            if (failing) {
                log.info("Index update {} no longer fails", name);
                failing = false;
            }

            // the update succeeded, so we can release the earlier checkpoint
            // otherwise the new checkpoint associated with the failed update
            // will get released in the finally block
            checkpointToRelease = beforeCheckpoint;

        } catch (CommitFailedException e) {
            if (e == CONCURRENT_UPDATE) {
                log.debug("Concurrent update detected in the {} index update", name);
            } else if (failing) {
                log.debug("The {} index update is still failing", name, e);
            } else {
                log.warn("The {} index update failed", name, e);
                failing = true;
            }

        } finally {
            if (checkpointToRelease != null) { // null during initial indexing
                store.release(checkpointToRelease);
            }
        }
    }
