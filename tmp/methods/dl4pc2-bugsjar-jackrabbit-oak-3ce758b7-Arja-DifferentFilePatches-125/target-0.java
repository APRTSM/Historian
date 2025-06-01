    public void close() {
        if (gcExecutor != null) {
            gcExecutor.shutdown();
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
