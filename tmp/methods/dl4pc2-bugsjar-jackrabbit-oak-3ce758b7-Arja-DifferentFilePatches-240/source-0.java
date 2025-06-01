    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
            gcExecutor.shutdown();
        }

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
    protected void verifyInitialized() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
                return ((PutTokenImpl) obj).id == id;
            }
            return super.equals(obj);
        }
