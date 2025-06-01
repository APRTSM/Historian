        public void run() {
            int delay = delaySupplier.get();
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
                    delay = delaySupplier.get();
                } else {
                    // node store not in use anymore
                    break;
                }
            }
        }
        NodeStoreTask(final DocumentNodeStore nodeStore,
                      final AtomicBoolean isDisposed,
                      Supplier<Integer> delay) {
            this.ref = new WeakReference<DocumentNodeStore>(nodeStore);
            this.isDisposed = isDisposed;
            if (delay == null) {
                delay = new Supplier<Integer>() {
                    @Override
                    public Integer get() {
                        DocumentNodeStore ns = ref.get();
                        return ns != null ? ns.getAsyncDelay() : 0;
                    }
                };
            }
            this.delaySupplier = delay;
        }
        NodeStoreTask(final DocumentNodeStore nodeStore,
                      final AtomicBoolean isDisposed) {
            this(nodeStore, isDisposed, null);
        }
        BackgroundLeaseUpdate(DocumentNodeStore nodeStore,
                              AtomicBoolean isDisposed) {
            super(nodeStore, isDisposed, Suppliers.ofInstance(1000));
        }
