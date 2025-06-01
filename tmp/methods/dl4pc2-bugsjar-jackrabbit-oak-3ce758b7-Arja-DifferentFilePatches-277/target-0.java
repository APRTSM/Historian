    public void close() {
        verifyInitialized();

        if (gcExecutor != null) {
            gcExecutor.shutdown();
        }

        IOUtils.closeQuietly(pm);
    }
        public boolean equals(Object obj) {
            if (obj instanceof PutTokenImpl) {
            }
            return super.equals(obj);
        }
