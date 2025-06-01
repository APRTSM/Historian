    void backgroundRead(boolean dispatchChange) {
        String id = Utils.getIdFromPath("/");
        NodeDocument doc = store.find(Collection.NODES, id, asyncDelay);
        if (doc == null) {
            return;
        }
        Map<Integer, Revision> lastRevMap = doc.getLastRev();

        Revision.RevisionComparator revisionComparator = getRevisionComparator();
        // the (old) head occurred first
        Revision headSeen = Revision.newRevision(0);
        // then we saw this new revision (from another cluster node)
        Revision otherSeen = Revision.newRevision(0);

        Map<Revision, Revision> externalChanges = Maps.newHashMap();
        for (Map.Entry<Integer, Revision> e : lastRevMap.entrySet()) {
            int machineId = e.getKey();
            if (machineId == clusterId) {
                // ignore own lastRev
                continue;
            }
            Revision r = e.getValue();
            Revision last = lastKnownRevision.get(machineId);
            if (last == null || r.compareRevisionTime(last) > 0) {
                lastKnownRevision.put(machineId, r);
                // OAK-2345
                // only consider as external change if
                // - the revision changed for the machineId
                // or
                // - the revision is within the time frame we remember revisions
                if (last != null
                        || r.getTimestamp() > revisionPurgeMillis())
                externalChanges.put(r, otherSeen);
            }
        }

        if (!externalChanges.isEmpty()) {
            // invalidate caches
            store.invalidateCache();
            // TODO only invalidate affected items
            docChildrenCache.invalidateAll();

            // make sure update to revision comparator is atomic
            // and no local commit is in progress
            backgroundOperationLock.writeLock().lock();
            try {
                // the latest revisions of the current cluster node
                // happened before the latest revisions of other cluster nodes
                revisionComparator.add(newRevision(), headSeen);
                // then we saw other revisions
                for (Map.Entry<Revision, Revision> e : externalChanges.entrySet()) {
                    revisionComparator.add(e.getKey(), e.getValue());
                }
                // the new head revision is after other revisions
                setHeadRevision(newRevision());
                if (dispatchChange) {
                    dispatcher.contentChanged(getRoot(), null);
                }
            } finally {
                backgroundOperationLock.writeLock().unlock();
            }
        }
        revisionComparator.purge(revisionPurgeMillis());
    }
    private static long revisionPurgeMillis() {
        return Revision.getCurrentTimestamp() - REMEMBER_REVISION_ORDER_MILLIS;
    }
