        NodeStoreTask(DocumentNodeStore nodeStore, AtomicBoolean isDisposed) {
            ref = new WeakReference<DocumentNodeStore>(nodeStore);
            delay = nodeStore.getAsyncDelay();
            this.isDisposed = isDisposed;
        }
        public void run() {
            while (delay != 0 && !isDisposed.get()) {
                synchronized (isDisposed) {
                    try {
                        isDisposed.wait(delay);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                DocumentNodeStore nodeStore = ref.get();
                if (nodeStore != null) {
                    try {
                        execute(nodeStore);
                    } catch (Throwable t) {
                        LOG.warn("Background operation failed: " + t.toString(), t);
                    }
                    delay = nodeStore.getAsyncDelay();
                } else {
                    // node store not in use anymore
                    break;
                }
            }
        }
        BackgroundLeaseUpdate(DocumentNodeStore nodeStore,
                              AtomicBoolean isDisposed) {
            super(nodeStore, isDisposed);
        }
