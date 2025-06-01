    void backgroundRead() {
        String id = Utils.getIdFromPath("/");
        NodeDocument doc = store.find(Collection.NODES, id, asyncDelay);
        if (doc == null) {
            return;
        }
        Map<Integer, Revision> lastRevMap = doc.getLastRev();

        Revision.RevisionComparator revisionComparator = getRevisionComparator();
        boolean hasNewRevisions = false;
        // the (old) head occurred first
        Revision headSeen = Revision.newRevision(0);
        // then we saw this new revision (from another cluster node)
        Revision otherSeen = Revision.newRevision(0);
        for (Map.Entry<Integer, Revision> e : lastRevMap.entrySet()) {
            int machineId = e.getKey();
            if (machineId == clusterId) {
                continue;
            }
            Revision r = e.getValue();
            Revision last = lastKnownRevision.get(machineId);
            if (last == null || r.compareRevisionTime(last) > 0) {
                lastKnownRevision.put(machineId, r);
                hasNewRevisions = true;
                revisionComparator.add(r, otherSeen);
            }
        }
        if (hasNewRevisions) {
            store.invalidateCache();
            // TODO only invalidate affected items
            docChildrenCache.invalidateAll();
            // add a new revision, so that changes are visible
            Revision r = Revision.newRevision(clusterId);
            // the latest revisions of the current cluster node
            // happened before the latest revisions of other cluster nodes
            revisionComparator.add(r, headSeen);
            // the head revision is after other revisions
            setHeadRevision(Revision.newRevision(clusterId));
        }
        revisionComparator.purge(Revision.getCurrentTimestamp() - REMEMBER_REVISION_ORDER_MILLIS);
    }
