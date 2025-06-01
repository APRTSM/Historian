    public synchronized void run() {
        log.debug("Running background index task {}", name);

        if (isAlreadyRunning(store, name)) {
            log.debug("Async job '{}' found to be already running. Skipping", name);
            return;
        }

        String checkpoint = store.checkpoint(lifetime);
        NodeState after = store.retrieve(checkpoint);
        if (after == null) {
            log.debug("Unable to retrieve checkpoint {}", checkpoint);
            return;
        }

        NodeBuilder builder = store.getRoot().builder();
        NodeBuilder async = builder.child(ASYNC);

        NodeState before = null;
        final PropertyState state = async.getProperty(name);
        if (state != null && state.getType() == STRING) {
            before = store.retrieve(state.getValue(STRING));
        }
        if (before == null) {
            before = MISSING_NODE;
        }

        AsyncUpdateCallback callback = new AsyncUpdateCallback();
        preAsyncRunStatsStats(indexStats);
        IndexUpdate indexUpdate = new IndexUpdate(provider, name, after,
                builder, callback);

        CommitFailedException exception = EditorDiff.process(indexUpdate,
                before, after);
        if (exception == null) {
            if (callback.dirty) {
                async.setProperty(name, checkpoint);
                try {
                    store.merge(builder, newCommitHook(name, state),
                            CommitInfo.EMPTY);
                } catch (CommitFailedException e) {
                    if (e != CONCURRENT_UPDATE) {
                        exception = e;
                    }
                }
                if (switchOnSync) {
                    reindexedDefinitions.addAll(indexUpdate
                            .getReindexedDefinitions());
                } else {
                    postAsyncRunStatsStatus(indexStats);
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
                    store.merge(builder, newCommitHook(name, state),
                            CommitInfo.EMPTY);
                    reindexedDefinitions.clear();
                    postAsyncRunStatsStatus(indexStats);
                } catch (CommitFailedException e) {
                    if (e != CONCURRENT_UPDATE) {
                        exception = e;
                    }
                }
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
