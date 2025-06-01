        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
        }

        cache.invalidateAll();

        initialized = false;
    }
