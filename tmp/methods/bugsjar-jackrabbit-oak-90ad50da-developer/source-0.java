    public <T extends Document> void update(Collection<T> collection,
                                            List<String> keys,
                                            UpdateOp updateOp) {
        log("update", keys, updateOp);
        UpdateUtils.assertUnconditional(updateOp);
        DBCollection dbCollection = getDBCollection(collection);
        QueryBuilder query = QueryBuilder.start(Document.ID).in(keys);
        // make sure we don't modify the original updateOp
        updateOp = updateOp.copy();
        DBObject update = createUpdate(updateOp);
        final long start = PERFLOG.start();
        try {
            Map<String, NodeDocument> cachedDocs = Collections.emptyMap();
            if (collection == Collection.NODES) {
                cachedDocs = Maps.newHashMap();
                for (String key : keys) {
                    cachedDocs.put(key, nodesCache.getIfPresent(key));
                }
            }
            try {
                dbCollection.update(query.get(), update, false, true);
                if (collection == Collection.NODES) {
                    // update cache
                    for (Entry<String, NodeDocument> entry : cachedDocs.entrySet()) {
                        // the cachedDocs is not empty, so the collection = NODES
                        Lock lock = nodeLocks.acquire(entry.getKey());
                        try {
                            if (entry.getValue() == null || entry.getValue() == NodeDocument.NULL) {
                                // make sure concurrently loaded document is
                                // invalidated
                                nodesCache.invalidate(entry.getKey());
                            } else {
                                NodeDocument newDoc = applyChanges(Collection.NODES, entry.getValue(), updateOp.shallowCopy(entry.getKey()));
                                nodesCache.replaceCachedDocument(entry.getValue(), newDoc);
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (MongoException e) {
                throw DocumentStoreException.convert(e);
            }
        } finally {
            PERFLOG.end(start, 1, "update");
        }
    }
    public CacheInvalidationStats invalidateCache(Iterable<String> keys) {
        LOG.debug("invalidateCache: start");
        final InvalidationResult result = new InvalidationResult();
        int size  = 0;

        final Iterator<String> it = keys.iterator();
        while(it.hasNext()) {
            // read chunks of documents only
            final List<String> ids = new ArrayList<String>(IN_CLAUSE_BATCH_SIZE);
            while(it.hasNext() && ids.size() < IN_CLAUSE_BATCH_SIZE) {
                final String id = it.next();
                if (nodesCache.getIfPresent(id) != null) {
                    // only add those that we actually do have cached
                    ids.add(id);
                }
            }
            size += ids.size();
            if (LOG.isTraceEnabled()) {
                LOG.trace("invalidateCache: batch size: {} of total so far {}",
                        ids.size(), size);
            }
            
            QueryBuilder query = QueryBuilder.start(Document.ID).in(ids);
            // Fetch only the modCount and id
            final BasicDBObject fields = new BasicDBObject(Document.ID, 1);
            fields.put(Document.MOD_COUNT, 1);
            
            DBCursor cursor = nodes.find(query.get(), fields);
            cursor.setReadPreference(ReadPreference.primary());
            result.queryCount++;

            Map<String, Number> modCounts = new HashMap<String, Number>();
            for (DBObject obj : cursor) {
                String id = (String) obj.get(Document.ID);
                Number modCount = (Number) obj.get(Document.MOD_COUNT);
                modCounts.put(id, modCount);
            }

            int invalidated = nodesCache.invalidateOutdated(modCounts);
            result.cacheEntriesProcessedCount += modCounts.size();
            result.invalidationCount += invalidated;
            result.upToDateCount = modCounts.size() - invalidated;
        }

        result.cacheSize = size;
        LOG.trace("invalidateCache: end. total: {}", size);
        return result;
    }
