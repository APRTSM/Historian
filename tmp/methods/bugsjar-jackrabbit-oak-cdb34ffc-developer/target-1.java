    private static boolean safeEnterWhen(Monitor monitor, Guard guard) {
        try {
            monitor.enterWhen(guard);
            return true;
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    private SegmentBufferWriter borrowWriter(Object key) {
        poolMonitor.enter();
        try {
            SegmentBufferWriter writer = writers.remove(key);
            if (writer == null) {
                writer = new SegmentBufferWriter(store, tracker, reader, version, getWriterId(wid), gcGeneration.get());
            } else if (writer.getGeneration() != gcGeneration.get()) {
                disposed.add(writer);
                writer = new SegmentBufferWriter(store, tracker, reader, version, getWriterId(wid), gcGeneration.get());
            }
            borrowed.add(writer);
            return writer;
        } finally {
            poolMonitor.leave();
        }
    }
    private Guard allReturned(final List<SegmentBufferWriter> toReturn) {
        return new Guard(poolMonitor) {

            @Override
            public boolean isSatisfied() {
                return disposed.containsAll(toReturn);
            }

        };
    }
    public void flush() throws IOException {
        List<SegmentBufferWriter> toFlush = newArrayList();
        List<SegmentBufferWriter> toReturn = newArrayList();

        poolMonitor.enter();
        try {
            // Collect all writers that are not currently in use and clear
            // the list so they won't get re-used anymore.
            toFlush.addAll(writers.values());
            writers.clear();

            // Collect all borrowed writers, which we need to wait for.
            // Clear the list so they will get disposed once returned.
            toReturn.addAll(borrowed);
            borrowed.clear();
        } finally {
            poolMonitor.leave();
        }

        // Wait for the return of the borrowed writers. This is the
        // case once all of them appear in the disposed set.
        if (safeEnterWhen(poolMonitor, allReturned(toReturn))) {
            try {
                // Collect all disposed writers and clear the list to mark them
                // as flushed.
                toFlush.addAll(toReturn);
                disposed.removeAll(toReturn);
            } finally {
                poolMonitor.leave();
            }
        }

        // Call flush from outside the pool monitor to avoid potential
        // deadlocks of that method calling SegmentStore.writeSegment
        for (SegmentBufferWriter writer : toFlush) {
            writer.flush();
        }
    }
    private void returnWriter(Object key, SegmentBufferWriter writer) {
        poolMonitor.enter();
        try {
            if (borrowed.remove(writer)) {
                checkState(writers.put(key, writer) == null);
            } else {
                // Defer flush this writer as it was borrowed while flush() was called.
                disposed.add(writer);
            }
        } finally {
            poolMonitor.leave();
        }
    }
    RecordId execute(@Nonnull WriteOperation writeOperation) throws IOException;

    /**
     * Flush any pending changes on any {@link SegmentBufferWriter} managed by this instance.
