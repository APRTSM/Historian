    void backgroundRead() {
        nodeStore.backgroundRead();
    }
    private void alignWithExternalRevisions(@Nonnull NodeDocument rootDoc) {
        Map<Integer, Revision> lastRevMap = checkNotNull(rootDoc).getLastRev();
        try {
            long externalTime = Utils.getMaxExternalTimestamp(lastRevMap.values(), clusterId);
            long localTime = clock.getTime();
            if (localTime < externalTime) {
                LOG.warn("Detected clock differences. Local time is '{}', " +
                                "while most recent external time is '{}'. " +
                                "Current _lastRev entries: {}",
                        new Date(localTime), new Date(externalTime), lastRevMap.values());
                double delay = ((double) externalTime - localTime) / 1000d;
                String msg = String.format("Background read will be delayed by %.1f seconds. " +
                        "Please check system time on cluster nodes.", delay);
                LOG.warn(msg);
                clock.waitUntil(externalTime + 1);
            } else if (localTime == externalTime) {
                // make sure local time is past external time
                // but only log at debug
                LOG.debug("Local and external time are equal. Waiting until local" +
                        "time is more recent than external reported time.");
                clock.waitUntil(externalTime + 1);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Background read interrupted", e);
        }
    }
    private void initializeHeadRevision(NodeDocument rootDoc) {
        checkState(headRevision == null);

        alignWithExternalRevisions(rootDoc);
        Map<Integer, Revision> lastRevMap = rootDoc.getLastRev();
        Revision seenAt = Revision.newRevision(0);
        long purgeMillis = revisionPurgeMillis();
        for (Map.Entry<Integer, Revision> entry : lastRevMap.entrySet()) {
            Revision r = entry.getValue();
            if (r.getTimestamp() > purgeMillis) {
                revisionComparator.add(r, seenAt);
            }
            if (entry.getKey() == clusterId) {
                continue;
            }
            lastKnownRevision.put(entry.getKey(), entry.getValue());
        }
        revisionComparator.purge(purgeMillis);
        setHeadRevision(newRevision());
    }
    BackgroundReadStats backgroundRead() {
        BackgroundReadStats stats = new BackgroundReadStats();
        long time = clock.getTime();
        String id = Utils.getIdFromPath("/");
        NodeDocument doc = store.find(Collection.NODES, id, asyncDelay);
        if (doc == null) {
            return stats;
        }
        alignWithExternalRevisions(doc);

        Revision.RevisionComparator revisionComparator = getRevisionComparator();
        // the (old) head occurred first
        Revision headSeen = Revision.newRevision(0);
        // then we saw this new revision (from another cluster node)
        Revision otherSeen = Revision.newRevision(0);

        StringSort externalSort = JournalEntry.newSorter();

        Map<Integer, Revision> lastRevMap = doc.getLastRev();
        try {
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
                            || r.getTimestamp() > revisionPurgeMillis()) {
                        externalChanges.put(r, otherSeen);
                    }
                    // collect external changes
                    if (last != null && externalSort != null) {
                        // add changes for this particular clusterId to the externalSort
                        try {
                            fillExternalChanges(externalSort, last, r, store);
                        } catch (IOException e1) {
                            LOG.error("backgroundRead: Exception while reading external changes from journal: " + e1, e1);
                            IOUtils.closeQuietly(externalSort);
                            externalSort = null;
                        }
                    }
                }
            }

            stats.readHead = clock.getTime() - time;
            time = clock.getTime();

            if (!externalChanges.isEmpty()) {
                // invalidate caches
                if (externalSort == null) {
                    // if no externalSort available, then invalidate the classic way: everything
                    stats.cacheStats = store.invalidateCache();
                    docChildrenCache.invalidateAll();
                } else {
                    try {
                        externalSort.sort();
                        stats.cacheStats = store.invalidateCache(pathToId(externalSort));
                        // OAK-3002: only invalidate affected items (using journal)
                        long origSize = docChildrenCache.size();
                        if (origSize == 0) {
                            // if docChildrenCache is empty, don't bother
                            // calling invalidateAll either way
                            // (esp calling invalidateAll(Iterable) will
                            // potentially iterate over all keys even though
                            // there's nothing to be deleted)
                            LOG.trace("backgroundRead: docChildrenCache nothing to invalidate");
                        } else {
                            // however, if the docChildrenCache is not empty,
                            // use the invalidateAll(Iterable) variant,
                            // passing it a Iterable<StringValue>, as that's
                            // what is contained in the cache
                            docChildrenCache.invalidateAll(asStringValueIterable(externalSort));
                            long newSize = docChildrenCache.size();
                            LOG.trace("backgroundRead: docChildrenCache invalidation result: orig: {}, new: {} ", origSize, newSize);
                        }
                    } catch (Exception ioe) {
                        LOG.error("backgroundRead: got IOException during external sorting/cache invalidation (as a result, invalidating entire cache): "+ioe, ioe);
                        stats.cacheStats = store.invalidateCache();
                        docChildrenCache.invalidateAll();
                    }
                }
                stats.cacheInvalidationTime = clock.getTime() - time;
                time = clock.getTime();

                // make sure update to revision comparator is atomic
                // and no local commit is in progress
                backgroundOperationLock.writeLock().lock();
                try {
                    stats.lock = clock.getTime() - time;

                    // the latest revisions of the current cluster node
                    // happened before the latest revisions of other cluster nodes
                    revisionComparator.add(newRevision(), headSeen);
                    // then we saw other revisions
                    for (Map.Entry<Revision, Revision> e : externalChanges.entrySet()) {
                        revisionComparator.add(e.getKey(), e.getValue());
                    }

                    Revision oldHead = headRevision;
                    // the new head revision is after other revisions
                    setHeadRevision(newRevision());
                    commitQueue.headRevisionChanged();
                    time = clock.getTime();
                    if (externalSort != null) {
                        // then there were external changes and reading them
                        // was successful -> apply them to the diff cache
                        try {
                            JournalEntry.applyTo(externalSort, diffCache, oldHead, headRevision);
                        } catch (Exception e1) {
                            LOG.error("backgroundRead: Exception while processing external changes from journal: {}", e1, e1);
                        }
                    }
                    stats.populateDiffCache = clock.getTime() - time;
                    time = clock.getTime();

                    dispatcher.contentChanged(getRoot().fromExternalChange(), null);
                } finally {
                    backgroundOperationLock.writeLock().unlock();
                }
                stats.dispatchChanges = clock.getTime() - time;
                time = clock.getTime();
            }
        } finally {
            IOUtils.closeQuietly(externalSort);
        }
        revisionComparator.purge(revisionPurgeMillis());
        stats.purge = clock.getTime() - time;

        return stats;
    }
    private void internalRunBackgroundReadOperations() {
        synchronized (backgroundReadMonitor) {
            long start = clock.getTime();
            // pull in changes from other cluster nodes
            BackgroundReadStats readStats = backgroundRead();
            long readTime = clock.getTime() - start;
            String msg = "Background read operations stats (read:{} {})";
            if (clock.getTime() - start > TimeUnit.SECONDS.toMillis(10)) {
                // log as info if it took more than 10 seconds
                LOG.info(msg, readTime, readStats);
            } else {
                LOG.debug(msg, readTime, readStats);
            }
        }
    }
    public DocumentNodeStore(DocumentMK.Builder builder) {
        this.blobStore = builder.getBlobStore();
        if (builder.isUseSimpleRevision()) {
            this.simpleRevisionCounter = new AtomicInteger(0);
        }
        DocumentStore s = builder.getDocumentStore();
        if (builder.getTiming()) {
            s = new TimingDocumentStoreWrapper(s);
        }
        if (builder.getLogging()) {
            s = new LoggingDocumentStoreWrapper(s);
        }
        this.changes = Collection.JOURNAL.newDocument(s);
        this.executor = builder.getExecutor();
        this.clock = builder.getClock();

        int cid = builder.getClusterId();
        cid = Integer.getInteger("oak.documentMK.clusterId", cid);
        clusterNodeInfo = ClusterNodeInfo.getInstance(s, cid);
        // TODO we should ensure revisions generated from now on
        // are never "older" than revisions already in the repository for
        // this cluster id
        cid = clusterNodeInfo.getId();

        if (builder.getLeaseCheck()) {
            s = new LeaseCheckDocumentStoreWrapper(s, clusterNodeInfo);
            clusterNodeInfo.setLeaseFailureHandler(builder.getLeaseFailureHandler());
        }
        this.store = s;
        this.clusterId = cid;
        this.revisionComparator = new Revision.RevisionComparator(clusterId);
        this.branches = new UnmergedBranches(getRevisionComparator());
        this.asyncDelay = builder.getAsyncDelay();
        this.versionGarbageCollector = new VersionGarbageCollector(
                this, builder.createVersionGCSupport());
        this.journalGarbageCollector = new JournalGarbageCollector(this);
        this.referencedBlobs = builder.createReferencedBlobs(this);
        this.lastRevRecoveryAgent = new LastRevRecoveryAgent(this,
                builder.createMissingLastRevSeeker());
        this.disableBranches = builder.isDisableBranches();
        this.missing = new DocumentNodeState(this, "MISSING", new Revision(0, 0, 0)) {
            @Override
            public int getMemory() {
                return 8;
            }
        };

        //TODO Make stats collection configurable as it add slight overhead

        nodeCache = builder.buildNodeCache(this);
        nodeCacheStats = new CacheStats(nodeCache, "Document-NodeState",
                builder.getWeigher(), builder.getNodeCacheSize());

        nodeChildrenCache = builder.buildChildrenCache();
        nodeChildrenCacheStats = new CacheStats(nodeChildrenCache, "Document-NodeChildren",
                builder.getWeigher(), builder.getChildrenCacheSize());

        docChildrenCache = builder.buildDocChildrenCache();
        docChildrenCacheStats = new CacheStats(docChildrenCache, "Document-DocChildren",
                builder.getWeigher(), builder.getDocChildrenCacheSize());

        diffCache = builder.getDiffCache();
        checkpoints = new Checkpoints(this);

        // check if root node exists
        NodeDocument rootDoc = store.find(NODES, Utils.getIdFromPath("/"));
        if (rootDoc == null) {
            // root node is missing: repository is not initialized
            Revision head = newRevision();
            Commit commit = new Commit(this, head, null, null);
            DocumentNodeState n = new DocumentNodeState(this, "/", head);
            commit.addNode(n);
            commit.applyToDocumentStore();
            // use dummy Revision as before
            commit.applyToCache(new Revision(0, 0, clusterId), false);
            setHeadRevision(commit.getRevision());
            // make sure _lastRev is written back to store
            backgroundWrite();
            rootDoc = store.find(NODES, Utils.getIdFromPath("/"));
            // at this point the root document must exist
            if (rootDoc == null) {
                throw new IllegalStateException("Root document does not exist");
            }
        } else {
            checkLastRevRecovery();
            initializeHeadRevision(rootDoc);
            // check if _lastRev for our clusterId exists
            if (!rootDoc.getLastRev().containsKey(clusterId)) {
                unsavedLastRevisions.put("/", headRevision);
                backgroundWrite();
            }
        }

        // Renew the lease because it may have been stale
        renewClusterIdLease();

        getRevisionComparator().add(headRevision, Revision.newRevision(0));

        // initialize branchCommits
        branches.init(store, this);

        dispatcher = new ChangeDispatcher(getRoot());
        commitQueue = new CommitQueue(this);
        String threadNamePostfix = "(" + clusterId + ")";
        batchCommitQueue = new BatchCommitQueue(store, revisionComparator);
        backgroundReadThread = new Thread(
                new BackgroundReadOperation(this, isDisposed),
                "DocumentNodeStore background read thread " + threadNamePostfix);
        backgroundReadThread.setDaemon(true);
        backgroundUpdateThread = new Thread(
                new BackgroundOperation(this, isDisposed),
                "DocumentNodeStore background update thread " + threadNamePostfix);
        backgroundUpdateThread.setDaemon(true);

        backgroundReadThread.start();
        backgroundUpdateThread.start();

        leaseUpdateThread = new Thread(new BackgroundLeaseUpdate(this, isDisposed),
                "DocumentNodeStore lease update thread " + threadNamePostfix);
        leaseUpdateThread.setDaemon(true);
        // OAK-3398 : make lease updating more robust by ensuring it
        // has higher likelihood of succeeding than other threads
        // on a very busy machine - so as to prevent lease timeout.
        leaseUpdateThread.setPriority(Thread.MAX_PRIORITY);
        leaseUpdateThread.start();

        this.mbean = createMBean();
        LOG.info("Initialized DocumentNodeStore with clusterNodeId: {} ({})", clusterId,
                getClusterNodeInfoDisplayString());
    }
