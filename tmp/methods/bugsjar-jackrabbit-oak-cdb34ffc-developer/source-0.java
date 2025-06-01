    public void flush() throws IOException {
        List<SegmentBufferWriter> toFlush = newArrayList();
        synchronized (this) {
            toFlush.addAll(writers.values());
            toFlush.addAll(disposed);
            writers.clear();
            disposed.clear();
            borrowed.clear();
        }
        // Call flush from outside a synchronized context to avoid
        // deadlocks of that method calling SegmentStore.writeSegment
        for (SegmentBufferWriter writer : toFlush) {
            writer.flush();
        }
    }
    private synchronized SegmentBufferWriter borrowWriter(Object key) {
        SegmentBufferWriter writer = writers.remove(key);
        if (writer == null) {
            writer = new SegmentBufferWriter(store, tracker, reader, version, getWriterId(wid), gcGeneration.get());
        } else if (writer.getGeneration() != gcGeneration.get()) {
            disposed.add(writer);
            writer = new SegmentBufferWriter(store, tracker, reader, version, getWriterId(wid), gcGeneration.get());
        }
        borrowed.add(writer);
        return writer;
    }
    private synchronized void returnWriter(Object key, SegmentBufferWriter writer) {
        if (borrowed.remove(writer)) {
            checkState(writers.put(key, writer) == null);
        } else {
            // Defer flush this writer as it was borrowed while flush() was called.
            disposed.add(writer);
        }
    }
