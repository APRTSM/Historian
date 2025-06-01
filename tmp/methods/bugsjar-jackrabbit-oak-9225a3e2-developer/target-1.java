    public int purgeUncommittedRevisions(RevisionContext context) {
        // only look at revisions in this document.
        // uncommitted revisions are not split off
        Map<Revision, String> valueMap = getLocalRevisions();
        UpdateOp op = new UpdateOp(getId(), false);
        int purgeCount = 0;
        for (Map.Entry<Revision, String> commit : valueMap.entrySet()) {
            if (!Utils.isCommitted(commit.getValue())) {
                Revision r = commit.getKey();
                if (r.getClusterId() == context.getClusterId()) {
                    purgeCount++;
                    op.removeMapEntry(REVISIONS, r);
                }
            }
        }

        if (op.hasChanges()) {
            store.findAndUpdate(Collection.NODES, op);
        }
        return purgeCount;
    }
    void init(DocumentStore store, RevisionContext context) {
        if (!initialized.compareAndSet(false, true)) {
            throw new IllegalStateException("already initialized");
        }
        NodeDocument doc = store.find(Collection.NODES, Utils.getIdFromPath("/"));
        if (doc == null) {
            return;
        }
        int purgeCount = doc.purgeUncommittedRevisions(context);
        if (purgeCount > 0) {
            log.info("Purged [{}] uncommitted branch revision entries", purgeCount);
        }
    }
