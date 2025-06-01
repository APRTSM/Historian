    public String branch(String trunkRevisionId) throws MicroKernelException {
        // create a private branch

        if (rep == null) {
            throw new IllegalStateException("this instance has already been disposed");
        }

        Id revId = trunkRevisionId == null ? getHeadRevisionId() : Id.fromString(trunkRevisionId);

        try {
            CommitBuilder cb = rep.getCommitBuilder(revId, "");
            return cb.doCommit(true).toString();
        } catch (Exception e) {
            throw new MicroKernelException(e);
        }
    }
