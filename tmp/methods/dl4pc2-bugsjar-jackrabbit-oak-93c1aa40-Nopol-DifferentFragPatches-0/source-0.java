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
