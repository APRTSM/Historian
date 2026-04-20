        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    public void close() {
        verifyInitialized();

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
