    public String getHeadRevision() throws MicroKernelException {
        if (rep == null) {
            throw new IllegalStateException("this instance has already been disposed");
        }
        return getHeadRevisionId().toString();
    }
