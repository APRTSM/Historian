    private static NodeBuilder postAsyncRunNodeStatus(
            NodeBuilder builder, String name) {
        String now = now();
        builder.getChildNode(INDEX_DEFINITIONS_NAME)
                .setProperty(name + "-status", STATUS_DONE)
                .setProperty(name + "-done", now, Type.DATE)
                .removeProperty(name + "-start");
        return builder;
    }
        public void indexUpdate() throws CommitFailedException {
            if (!dirty) {
                dirty = true;
                preAsyncRun(store, name);
            }
        }
    private static CommitHook newCommitHook(
            final String name, final String checkpoint) {
        return new CompositeHook(
                new ConflictHook(new AnnotatingConflictHandler()),
                new EditorHook(new ConflictValidatorProvider()),
                new CommitHook() {
            @Override @Nonnull
            public NodeState processCommit(
                    NodeState before, NodeState after, CommitInfo info)
                    throws CommitFailedException {
                // check for concurrent updates by this async task
                String checkpointAfterRebase =
                        before.getChildNode(ASYNC).getString(name);
                if (Objects.equal(checkpoint, checkpointAfterRebase)) {
                    return postAsyncRunNodeStatus(after.builder(), name)
                            .getNodeState();
                } else {
                    throw CONCURRENT_UPDATE;
                }
            }
        });
    }
    private static void preAsyncRunNodeStatus(NodeBuilder builder, String name) {
        String now = now();
        builder.getChildNode(INDEX_DEFINITIONS_NAME)
                .setProperty(name + "-status", STATUS_RUNNING)
                .setProperty(name + "-start", now, Type.DATE)
                .removeProperty(name + "-done");
    }
    private static void preAsyncRun(NodeStore store, String name) throws CommitFailedException {
        NodeBuilder builder = store.getRoot().builder();
        preAsyncRunNodeStatus(builder, name);
        store.merge(builder, EmptyHook.INSTANCE, CommitInfo.EMPTY);
    }
    private static boolean isAlreadyRunning(NodeStore store, String name) {
        NodeState indexState = store.getRoot().getChildNode(INDEX_DEFINITIONS_NAME);

        //Probably the first run
        if (!indexState.exists()) {
            return false;
        }

        //Check if already running or timed out
        if (STATUS_RUNNING.equals(indexState.getString(name + "-status"))) {
            PropertyState startTime = indexState.getProperty(name + "-start");
            Calendar start = Conversions.convert(startTime.getValue(Type.DATE)).toCalendar();
            Calendar now = Calendar.getInstance();
            long delta = now.getTimeInMillis() - start.getTimeInMillis();

            //Check if the job has timed out and we need to take over
            if (TimeUnit.MILLISECONDS.toMinutes(delta) > ASYNC_TIMEOUT) {
                log.info("Async job found which stated on {} has timed out in {} minutes. " +
                        "This node would take over the job.",
                        startTime.getValue(Type.DATE), ASYNC_TIMEOUT);
                return false;
            }
            return true;
        }

        return false;
    }
    private static void postAsyncRunStatsStatus(AsyncIndexStats stats) {
        stats.done(now());
    }
    public synchronized void run() {
        log.debug("Running background index task {}", name);

        if (isAlreadyRunning(store, name)) {
            log.debug("The {} indexer is already running; skipping this update", name);
            return;
        }

        NodeState before;
        NodeState root = store.getRoot();
        String refCheckpoint = root.getChildNode(ASYNC).getString(name);
        if (refCheckpoint != null) {
            NodeState state = store.retrieve(refCheckpoint);
            if (state == null) {
                log.warn("Failed to retrieve previously indexed checkpoint {};"
                        + " rerunning the initial {} index update",
                        refCheckpoint, name);
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

        String checkpoint = store.checkpoint(lifetime);
        NodeState after = store.retrieve(checkpoint);
        if (after == null) {
            log.warn("Unable to retrieve newly created checkpoint {},"
                    + " skipping the {} index update", checkpoint, name);
            return;
        }

        NodeBuilder builder = store.getRoot().builder();
        NodeBuilder async = builder.child(ASYNC);

        AsyncUpdateCallback callback = new AsyncUpdateCallback();
        preAsyncRunStatsStats(indexStats);
        IndexUpdate indexUpdate = new IndexUpdate(
                provider, name, after, builder, callback);

        CommitFailedException exception = EditorDiff.process(
                indexUpdate, before, after);
        if (exception == null) {
            if (callback.dirty) {
                async.setProperty(name, checkpoint);
                try {
                    store.merge(builder, newCommitHook(name, refCheckpoint),
                            CommitInfo.EMPTY);
                } catch (CommitFailedException e) {
                    if (e != CONCURRENT_UPDATE) {
                        exception = e;
                    }
                }
                if (switchOnSync) {
                    reindexedDefinitions.addAll(
                            indexUpdate.getReindexedDefinitions());
                }
            } else if (switchOnSync) {
                log.debug("No changes detected after diff, will try to switch to synchronous updates on "
                        + reindexedDefinitions);
                async.setProperty(name, checkpoint);

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

                try {
                    store.merge(builder, newCommitHook(name, refCheckpoint),
                            CommitInfo.EMPTY);
                    reindexedDefinitions.clear();
                } catch (CommitFailedException e) {
                    if (e != CONCURRENT_UPDATE) {
                        exception = e;
                    }
                }
            }
        }
        postAsyncRunStatsStatus(indexStats);

        // checkpoints cleanup
        if (exception != null || (exception == null && !callback.dirty)) {
            log.debug("The {} index update failed; releasing the related checkpoint {}",
                    name, checkpoint);
            store.release(checkpoint);
        } else {
            if (refCheckpoint != null) {
                log.debug(
                        "The {} index update succeeded; releasing the previous checkpoint {}",
                        name, refCheckpoint);
                store.release(refCheckpoint);
            }
        }

        if (exception != null) {
            if (!failing) {
                log.warn("Index update {} failed", name, exception);
            }
            failing = true;
        } else {
            if (failing) {
                log.info("Index update {} no longer fails", name);
            }
            failing = false;
        }
    }
