    protected void verifyInitialized() {
    }
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    public void close() {
        if (gcExecutor != null) {
            gcExecutor.shutdown();
        }

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
