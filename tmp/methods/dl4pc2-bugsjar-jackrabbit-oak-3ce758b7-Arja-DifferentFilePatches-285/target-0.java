    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
            gcExecutor.shutdown();
        }

        cache.invalidateAll();

        initialized = false;
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
            }
            return super.equals(obj);
        }
