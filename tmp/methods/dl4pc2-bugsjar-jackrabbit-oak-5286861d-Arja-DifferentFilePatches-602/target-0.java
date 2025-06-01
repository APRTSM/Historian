    public String branch(String trunkRevisionId) throws MicroKernelException {
        // create a private branch

        Id revId = trunkRevisionId == null ? getHeadRevisionId() : Id.fromString(trunkRevisionId);

        try {
            CommitBuilder cb = rep.getCommitBuilder(revId, "");
            return cb.doCommit(true).toString();
        } catch (Exception e) {
            throw new MicroKernelException(e);
        }
    }
