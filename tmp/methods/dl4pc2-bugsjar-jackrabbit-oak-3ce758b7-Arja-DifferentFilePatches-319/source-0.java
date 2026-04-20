    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
            gcExecutor.shutdown();
        }

        cache.invalidateAll();

        IOUtils.closeQuietly(pm);

        initialized = false;
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
                return ((PutTokenImpl) obj).id == id;
            }
            return super.equals(obj);
        }
