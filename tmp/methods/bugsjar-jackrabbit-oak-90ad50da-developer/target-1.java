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

            Map<String, Number> modCounts = getModCounts(ids);
            result.queryCount++;

            int invalidated = nodesCache.invalidateOutdated(modCounts);
            result.cacheEntriesProcessedCount += modCounts.size();
            result.invalidationCount += invalidated;
            result.upToDateCount = modCounts.size() - invalidated;
        }

        result.cacheSize = size;
        LOG.trace("invalidateCache: end. total: {}", size);
        return result;
    }
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
                    Map<String, Number> modCounts = getModCounts(filterValues(cachedDocs, notNull()).keySet());
                    // update cache
                    for (Entry<String, NodeDocument> entry : cachedDocs.entrySet()) {
                        // the cachedDocs is not empty, so the collection = NODES
                        Lock lock = nodeLocks.acquire(entry.getKey());
                        try {
                            Number postUpdateModCount = modCounts.get(entry.getKey());
                            if (postUpdateModCount != null
                                    && entry.getValue() != null
                                    && entry.getValue() != NodeDocument.NULL
                                    && (postUpdateModCount.longValue() - 1) == entry.getValue().getModCount()) {
                                // post update modCount is one higher than
                                // what we currently see in the cache. we can
                                // replace the cached document
                                NodeDocument newDoc = applyChanges(Collection.NODES, entry.getValue(), updateOp.shallowCopy(entry.getKey()));
                                nodesCache.replaceCachedDocument(entry.getValue(), newDoc);
                            } else {
                                // make sure concurrently loaded document is
                                // invalidated
                                nodesCache.invalidate(entry.getKey());
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            } catch (MongoException e) {
                // some documents may still have been updated
                // invalidate all documents affected by this update call
                for (String k : keys) {
                    nodesCache.invalidate(k);
                }
                throw DocumentStoreException.convert(e);
            }
        } finally {
            PERFLOG.end(start, 1, "update");
        }
    }
    private Map<String, Number> getModCounts(Iterable<String> keys)
            throws MongoException {
        QueryBuilder query = QueryBuilder.start(Document.ID).in(keys);
        // Fetch only the modCount and id
        final BasicDBObject fields = new BasicDBObject(Document.ID, 1);
        fields.put(Document.MOD_COUNT, 1);

        DBCursor cursor = nodes.find(query.get(), fields);
        cursor.setReadPreference(ReadPreference.primary());

        Map<String, Number> modCounts = Maps.newHashMap();
        for (DBObject obj : cursor) {
            String id = (String) obj.get(Document.ID);
            Number modCount = (Number) obj.get(Document.MOD_COUNT);
            modCounts.put(id, modCount);
        }
        return modCounts;
    }
    private <T extends Document> void internalUpdate(Collection<T> collection, List<String> ids, UpdateOp update) {

        if (isAppendableUpdate(update) && !requiresPreviousState(update)) {
            Operation modOperation = update.getChanges().get(MODIFIEDKEY);
            long modified = getModifiedFromOperation(modOperation);
            boolean modifiedIsConditional = modOperation == null || modOperation.type != UpdateOp.Operation.Type.SET;
            String appendData = ser.asString(update);

            for (List<String> chunkedIds : Lists.partition(ids, CHUNKSIZE)) {

                Set<QueryContext> seenQueryContext = Collections.emptySet();
                Map<String, NodeDocument> cachedDocs = Collections.emptyMap();

                if (collection == Collection.NODES) {
                    // remember what we already have in the cache
                    cachedDocs = new HashMap<String, NodeDocument>();
                    for (String key : chunkedIds) {
                        cachedDocs.put(key, nodesCache.getIfPresent(key));
                    }

                    // keep concurrently running queries from updating
                    // the cache entry for this key
                    seenQueryContext = new HashSet<QueryContext>();
                    for (QueryContext qc : qmap.values()) {
                        qc.addKeys(chunkedIds);
                        seenQueryContext.add(qc);
                    }
                    for (String id : chunkedIds) {
                        nodesCache.invalidate(id);
                    }
                }

                Connection connection = null;
                RDBTableMetaData tmd = getTable(collection);
                boolean success = false;
                try {
                    connection = this.ch.getRWConnection();
                    success = db.batchedAppendingUpdate(connection, tmd, chunkedIds, modified, modifiedIsConditional, appendData);
                    connection.commit();
                } catch (SQLException ex) {
                    success = false;
                    this.ch.rollbackConnection(connection);
                } finally {
                    this.ch.closeConnection(connection);
                }
                if (success) {
                    if (collection == Collection.NODES) {
                        // keep concurrently running queries from updating
                        // the cache entry for this key
                        for (QueryContext qc : qmap.values()) {
                            if (!seenQueryContext.contains(qc)) {
                                qc.addKeys(chunkedIds);
                            }
                        }
                        for (String id : chunkedIds) {
                            nodesCache.invalidate(id);
                        }
                    }
                } else {
                    for (String id : chunkedIds) {
                        UpdateOp up = update.copy();
                        up = up.shallowCopy(id);
                        internalCreateOrUpdate(collection, up, false, true);
                    }
                }
            }
        } else {
            for (String id : ids) {
                UpdateOp up = update.copy();
                up = up.shallowCopy(id);
                internalCreateOrUpdate(collection, up, false, true);
            }
        }
    }
