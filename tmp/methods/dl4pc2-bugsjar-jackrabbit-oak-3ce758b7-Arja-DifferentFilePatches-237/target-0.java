    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
        }

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
    protected void verifyInitialized() {
    }
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
