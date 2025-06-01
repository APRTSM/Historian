    public static void setModified(@Nonnull UpdateOp op,
                                   @Nonnull Revision revision) {
        checkNotNull(op).set(MODIFIED_IN_SECS, getModifiedInSecs(checkNotNull(revision).getTimestamp()));
    }
