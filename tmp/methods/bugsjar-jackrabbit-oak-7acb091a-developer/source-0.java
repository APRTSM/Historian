    private static boolean markCommitRoot(@Nonnull Map<String, Object> document,
                                          @Nonnull String revision,
                                          @Nonnull DocumentStore store) {
        @SuppressWarnings("unchecked")
        Map<String, Integer> commitRoots = (Map<String, Integer>) document.get(UpdateOp.COMMIT_ROOT);
        if (commitRoots != null) {
            Integer depth = commitRoots.get(revision);
            if (depth != null) {
                String p = Utils.getPathFromId((String) document.get(UpdateOp.ID));
                String commitRootPath = PathUtils.getAncestorPath(p, PathUtils.getDepth(p) - depth);
                UpdateOp op = new UpdateOp(commitRootPath,
                        Utils.getIdFromPath(commitRootPath), false);
                op.setMapEntry(UpdateOp.COLLISIONS, revision, true);
                // TODO: detect concurrent commit of previously un-merged changes
                // TODO: check _commitRoot for revision is not 'true'
                store.createOrUpdate(DocumentStore.Collection.NODES, op);
                LOG.debug("Marked collision on: {} for {} ({})",
                        new Object[]{commitRootPath, p, revision});
                return true;
            }
        }
        return false;
    }
    boolean mark(DocumentStore store) {
        if (markCommitRoot(document, theirRev, store)) {
            return true;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> revisions = (Map<String, String>) document.get(UpdateOp.REVISIONS);
        if (revisions.containsKey(theirRev)) {
            String value = revisions.get(theirRev);
            if ("true".equals(value)) {
                // their commit wins, we have to mark ourRev
                Map<String, Object> newDoc = Utils.newMap();
                Utils.deepCopyMap(document, newDoc);
                MemoryDocumentStore.applyChanges(newDoc, ourOp);
                if (markCommitRoot(newDoc, ourRev, store)) {
                    return true;
                }
            }
        }
        return true;
    }
