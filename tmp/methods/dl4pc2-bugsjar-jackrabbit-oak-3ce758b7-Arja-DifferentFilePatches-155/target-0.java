    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
        }

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
            }
            return super.equals(obj);
        }
