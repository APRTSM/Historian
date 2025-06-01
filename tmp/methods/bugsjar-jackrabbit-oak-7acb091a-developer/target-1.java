    private static void throwNoCommitRootException(@Nonnull String revision,
                                                   @Nonnull Map<String, Object> document)
                                                           throws MicroKernelException {
        throw new MicroKernelException("No commit root for revision: "
                + revision + ", document: " + Utils.formatDocument(document));
    }
    void mark(DocumentStore store) throws MicroKernelException {
        // first try to mark their revision
        if (markCommitRoot(document, theirRev, store)) {
            return;
        }
        // their commit wins, we have to mark ourRev
        Map<String, Object> newDoc = Utils.newMap();
        Utils.deepCopyMap(document, newDoc);
        MemoryDocumentStore.applyChanges(newDoc, ourOp);
        if (!markCommitRoot(newDoc, ourRev, store)) {
            throw new MicroKernelException("Unable to annotate our revision "
                    + "with collision marker. Our revision: " + ourRev
                    + ", document:\n" + Utils.formatDocument(newDoc));
        }
    }
    private static boolean markCommitRoot(@Nonnull Map<String, Object> document,
                                          @Nonnull String revision,
                                          @Nonnull DocumentStore store) {
        String p = Utils.getPathFromId((String) document.get(UpdateOp.ID));
        String commitRootPath = null;
        // first check if we can mark the commit with the given revision
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions != null && revisions.containsKey(revision)) {
            String value = revisions.get(revision);
            if ("true".equals(value)) {
                // already committed
                return false;
            } else {
                // node is also commit root, but not yet committed
                // i.e. a branch commit, which is not yet merged
                commitRootPath = p;
            }
        } else {
            // next look at commit root
            @SuppressWarnings("unchecked")
            Map<String, Integer> commitRoots = (Map<String, Integer>) document.get(UpdateOp.COMMIT_ROOT);
            if (commitRoots != null) {
                Integer depth = commitRoots.get(revision);
                if (depth != null) {
                    commitRootPath = PathUtils.getAncestorPath(p, PathUtils.getDepth(p) - depth);
                } else {
                    throwNoCommitRootException(revision, document);
                }
            } else {
                throwNoCommitRootException(revision, document);
            }
        }
        // at this point we have a commitRootPath
        UpdateOp op = new UpdateOp(commitRootPath,
                Utils.getIdFromPath(commitRootPath), false);
        document = store.find(Collection.NODES, op.getKey());
        // check commit status of revision
        if (isCommitted(revision, document)) {
            return false;
        }
        op.setMapEntry(UpdateOp.COLLISIONS, revision, true);
        document = store.createOrUpdate(DocumentStore.Collection.NODES, op);
        // check again on old document right before our update was applied
        if (isCommitted(revision, document)) {
            return false;
        }
        // otherwise collision marker was set successfully
        LOG.debug("Marked collision on: {} for {} ({})",
                new Object[]{commitRootPath, p, revision});
        return true;
    }
    private static boolean isCommitted(String revision, Map<String, Object> document) {
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions != null && revisions.containsKey(revision)) {
            String value = revisions.get(revision);
            return "true".equals(value);
        }
        return false;
    }
    abstract void concurrentModification(Revision other);
