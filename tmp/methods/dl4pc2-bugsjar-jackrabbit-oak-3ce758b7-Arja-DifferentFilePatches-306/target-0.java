        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
            }
            return super.equals(obj);
        }
    public void close() {
        verifyInitialized();

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
    protected void verifyInitialized() {
    }
