    public String reset(@Nonnull String branchRevisionId,
                        @Nonnull String ancestorRevisionId)
            throws DocumentStoreException {
        RevisionVector branch = RevisionVector.fromString(branchRevisionId);
        if (!branch.isBranch()) {
            throw new DocumentStoreException("Not a branch revision: " + branchRevisionId);
        }
        RevisionVector ancestor = RevisionVector.fromString(ancestorRevisionId);
        if (!ancestor.isBranch()) {
            throw new DocumentStoreException("Not a branch revision: " + ancestorRevisionId);
        }
        try {
            return nodeStore.reset(branch, ancestor, null).toString();
        } catch (DocumentStoreException e) {
            throw new DocumentStoreException(e);
        }
    }
