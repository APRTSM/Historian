    BackgroundWriteStats backgroundWrite() {
        return unsavedLastRevisions.persist(this, new UnsavedModifications.Snapshot() {
            @Override
            public void acquiring(Revision mostRecent) {
                if (store.create(JOURNAL,
                        singletonList(changes.asUpdateOp(mostRecent)))) {
                    changes = JOURNAL.newDocument(getDocumentStore());
                }
            }
        }, backgroundOperationLock.writeLock());
    }
    public int recover(Iterator<NodeDocument> suspects,
                       int clusterId, boolean dryRun) {
        UnsavedModifications unsaved = new UnsavedModifications();
        UnsavedModifications unsavedParents = new UnsavedModifications();

        //Map of known last rev of checked paths
        Map<String, Revision> knownLastRevOrModification = MapFactory.getInstance().create();
        final DocumentStore docStore = nodeStore.getDocumentStore();
        final JournalEntry changes = JOURNAL.newDocument(docStore);

        long count = 0;
        while (suspects.hasNext()) {
            NodeDocument doc = suspects.next();
            count++;
            if (count % 100000 == 0) {
                log.info("Scanned {} suspects so far...", count);
            }

            Revision currentLastRev = doc.getLastRev().get(clusterId);

            // 1. determine last committed modification on document
            Revision lastModifiedRev = determineLastModification(doc, clusterId);

            Revision lastRevForParents = Utils.max(lastModifiedRev, currentLastRev);
            // remember the higher of the two revisions. this is the
            // most recent revision currently obtained from either a
            // _lastRev entry or an explicit modification on the document
            if (lastRevForParents != null) {
                knownLastRevOrModification.put(doc.getPath(), lastRevForParents);
            }

            //If both currentLastRev and lostLastRev are null it means
            //that no change is done by suspect cluster on this document
            //so nothing needs to be updated. Probably it was only changed by
            //other cluster nodes. If this node is parent of any child node which
            //has been modified by cluster then that node roll up would
            //add this node path to unsaved

            //2. Update lastRev for parent paths aka rollup
            if (lastRevForParents != null) {
                String path = doc.getPath();
                changes.modified(path); // track all changes
                while (true) {
                    if (PathUtils.denotesRoot(path)) {
                        break;
                    }
                    path = PathUtils.getParentPath(path);
                    unsavedParents.put(path, lastRevForParents);
                }
            }
        }

        for (String parentPath : unsavedParents.getPaths()) {
            Revision calcLastRev = unsavedParents.get(parentPath);
            Revision knownLastRev = knownLastRevOrModification.get(parentPath);
            if (knownLastRev == null) {
                // we don't know when the document was last modified with
                // the given clusterId. need to read from store
                String id = Utils.getIdFromPath(parentPath);
                NodeDocument doc = docStore.find(NODES, id);
                if (doc != null) {
                    Revision lastRev = doc.getLastRev().get(clusterId);
                    Revision lastMod = determineLastModification(doc, clusterId);
                    knownLastRev = Utils.max(lastRev, lastMod);
                } else {
                    log.warn("Unable to find document: {}", id);
                    continue;
                }
            }

            //Copy the calcLastRev of parent only if they have changed
            //In many case it might happen that parent have consistent lastRev
            //This check ensures that unnecessary updates are not made
            if (knownLastRev == null
                    || calcLastRev.compareRevisionTime(knownLastRev) > 0) {
                unsaved.put(parentPath, calcLastRev);
            }
        }

        // take the root's lastRev
        final Revision lastRootRev = unsaved.get("/");

        //Note the size before persist as persist operation
        //would empty the internal state
        int size = unsaved.getPaths().size();
        String updates = unsaved.toString();

        if (dryRun) {
            log.info("Dry run of lastRev recovery identified [{}] documents for " +
                    "cluster node [{}]: {}", size, clusterId, updates);
        } else {
            //UnsavedModifications is designed to be used in concurrent
            //access mode. For recovery case there is no concurrent access
            //involve so just pass a new lock instance

            // the lock uses to do the persisting is a plain reentrant lock
            // thus it doesn't matter, where exactly the check is done
            // as to whether the recovered lastRev has already been
            // written to the journal.
            unsaved.persist(nodeStore, new UnsavedModifications.Snapshot() {

                @Override
                public void acquiring(Revision mostRecent) {
                    if (lastRootRev == null) {
                        // this should never happen - when unsaved has no changes
                        // that is reflected in the 'map' to be empty - in that
                        // case 'persist()' quits early and never calls
                        // acquiring() here.
                        //
                        // but even if it would occur - if we have no lastRootRev
                        // then we cannot and probably don't have to persist anything
                        return;
                    }

                    final String id = JournalEntry.asId(lastRootRev); // lastRootRev never null at this point
                    final JournalEntry existingEntry = docStore.find(Collection.JOURNAL, id);
                    if (existingEntry != null) {
                        // then the journal entry was already written - as can happen if
                        // someone else (or the original instance itself) wrote the
                        // journal entry, then died.
                        // in this case, don't write it again.
                        // hence: nothing to be done here. return.
                        return;
                    }

                    // otherwise store a new journal entry now
                    docStore.create(JOURNAL, singletonList(changes.asUpdateOp(lastRootRev)));
                }
            }, new ReentrantLock());

            log.info("Updated lastRev of [{}] documents while performing lastRev recovery for " +
                    "cluster node [{}]: {}", size, clusterId, updates);
        }

        return size;
    }
    private Revision getMostRecentRevision() {
        // use revision of root document
        Revision rev = map.get("/");
        // otherwise find most recent
        if (rev == null) {
            for (Revision r : map.values()) {
                rev = Utils.max(rev, r);
            }
        }
        return rev;
    }
    public BackgroundWriteStats persist(@Nonnull DocumentNodeStore store,
                                        @Nonnull Snapshot snapshot,
                                        @Nonnull Lock lock) {
        BackgroundWriteStats stats = new BackgroundWriteStats();
        if (map.size() == 0) {
            return stats;
        }
        checkNotNull(store);
        checkNotNull(lock);

        Clock clock = store.getClock();

        long time = clock.getTime();
        // get a copy of the map while holding the lock
        lock.lock();
        stats.lock = clock.getTime() - time;
        time = clock.getTime();
        Map<String, Revision> pending;
        try {
            snapshot.acquiring(getMostRecentRevision());
            pending = Maps.newTreeMap(PathComparator.INSTANCE);
            pending.putAll(map);
        } finally {
            lock.unlock();
        }
        stats.num = pending.size();
        UpdateOp updateOp = null;
        Revision lastRev = null;
        PeekingIterator<String> paths = Iterators.peekingIterator(
                pending.keySet().iterator());
        int i = 0;
        ArrayList<String> pathList = new ArrayList<String>();
        while (paths.hasNext()) {
            String p = paths.peek();
            Revision r = pending.get(p);

            int size = pathList.size();
            if (updateOp == null) {
                // create UpdateOp
                Commit commit = new Commit(store, r, null, null);
                updateOp = commit.getUpdateOperationForNode(p);
                NodeDocument.setLastRev(updateOp, r);
                lastRev = r;
                pathList.add(p);
                paths.next();
                i++;
            } else if (r.equals(lastRev)) {
                // use multi update when possible
                pathList.add(p);
                paths.next();
                i++;
            }
            // call update if any of the following is true:
            // - this is the second-to-last or last path (update last path, the
            //   root document, individually)
            // - revision is not equal to last revision (size of ids didn't change)
            // - the update limit is reached
            if (i + 2 > pending.size()
                    || size == pathList.size()
                    || pathList.size() >= BACKGROUND_MULTI_UPDATE_LIMIT) {
                List<String> ids = new ArrayList<String>();
                for (String path : pathList) {
                    ids.add(Utils.getIdFromPath(path));
                }
                store.getDocumentStore().update(NODES, ids, updateOp);
                LOG.debug("Updated _lastRev to {} on {}", lastRev, ids);
                for (String path : pathList) {
                    map.remove(path, lastRev);
                }
                pathList.clear();
                updateOp = null;
                lastRev = null;
            }
        }
        Revision writtenRootRev = pending.get("/");
        if (writtenRootRev != null) {
            int cid = writtenRootRev.getClusterId();
            if (store.getDocumentStore().find(org.apache.jackrabbit.oak.plugins.document.Collection.CLUSTER_NODES, String.valueOf(cid)) != null) {
                UpdateOp update = new UpdateOp(String.valueOf(cid), false);
                update.equals(Document.ID, null, String.valueOf(cid));
                update.set(ClusterNodeInfo.LAST_WRITTEN_ROOT_REV_KEY, writtenRootRev.toString());
                store.getDocumentStore().findAndUpdate(org.apache.jackrabbit.oak.plugins.document.Collection.CLUSTER_NODES, update);
            }
        }

        stats.write = clock.getTime() - time;
        return stats;
    }
