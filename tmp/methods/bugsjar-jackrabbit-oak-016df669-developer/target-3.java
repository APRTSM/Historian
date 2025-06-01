    public static void setModified(@Nonnull UpdateOp op,
                                   @Nonnull Revision revision) {
        checkNotNull(op).max(MODIFIED_IN_SECS, getModifiedInSecs(checkNotNull(revision).getTimestamp()));
    }
    public void increment(@Nonnull String property, long value) {
        Operation op = new Operation(Operation.Type.INCREMENT, value);
        changes.put(new Key(property, null), op);
    }
    <T> void max(String property, Comparable<T> value) {
        Operation op = new Operation(Operation.Type.MAX, value);
        changes.put(new Key(property, null), op);
    }
    void set(String property, Object value) {
        Operation op = new Operation(Operation.Type.SET, value);
        changes.put(new Key(property, null), op);
    }
        public Operation getReverse() {
            Operation reverse = null;
            switch (type) {
            case INCREMENT:
                reverse = new Operation(Type.INCREMENT, -(Long) value);
                break;
            case SET:
            case MAX:
            case REMOVE_MAP_ENTRY:
            case CONTAINS_MAP_ENTRY:
                // nothing to do
                break;
            case SET_MAP_ENTRY:
                reverse = new Operation(Type.REMOVE_MAP_ENTRY, null);
                break;
            }
            return reverse;
        }
    void setMapEntry(@Nonnull String property, @Nonnull Revision revision, Object value) {
        Operation op = new Operation(Operation.Type.SET_MAP_ENTRY, value);
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
    void containsMapEntry(@Nonnull String property,
                          @Nonnull Revision revision,
                          boolean exists) {
        if (isNew) {
            throw new IllegalStateException("Cannot use containsMapEntry() on new document");
        }
        Operation op = new Operation(Operation.Type.CONTAINS_MAP_ENTRY, exists);
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
    public void removeMapEntry(@Nonnull String property, @Nonnull Revision revision) {
        Operation op = new Operation(Operation.Type.REMOVE_MAP_ENTRY, null);
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
        Operation(Type type, Object value) {
            this.type = checkNotNull(type);
            this.value = value;
        }
    public static void applyChanges(@Nonnull Document doc,
                                    @Nonnull UpdateOp update,
                                    @Nonnull Comparator<Revision> comparator) {
        for (Entry<Key, Operation> e : checkNotNull(update).getChanges().entrySet()) {
            Key k = e.getKey();
            Operation op = e.getValue();
            switch (op.type) {
                case SET: {
                    doc.put(k.toString(), op.value);
                    break;
                }
                case MAX: {
                    Comparable newValue = (Comparable) op.value;
                    Object old = doc.get(k.toString());
                    //noinspection unchecked
                    if (old == null || newValue.compareTo(old) > 0) {
                        doc.put(k.toString(), op.value);
                    }
                    break;
                }
                case INCREMENT: {
                    Object old = doc.get(k.toString());
                    Long x = (Long) op.value;
                    if (old == null) {
                        old = 0L;
                    }
                    doc.put(k.toString(), ((Long) old) + x);
                    break;
                }
                case SET_MAP_ENTRY: {
                    Object old = doc.get(k.getName());
                    @SuppressWarnings("unchecked")
                    Map<Revision, Object> m = (Map<Revision, Object>) old;
                    if (m == null) {
                        m = new TreeMap<Revision, Object>(comparator);
                        doc.put(k.getName(), m);
                    }
                    if (k.getRevision() == null) {
                        throw new IllegalArgumentException("Cannot set map entry " + k.getName() + " with null revision");
                    }
                    m.put(k.getRevision(), op.value);
                    break;
                }
                case REMOVE_MAP_ENTRY: {
                    Object old = doc.get(k.getName());
                    @SuppressWarnings("unchecked")
                    Map<Revision, Object> m = (Map<Revision, Object>) old;
                    if (m != null) {
                        m.remove(k.getRevision());
                    }
                    break;
                }
                case CONTAINS_MAP_ENTRY:
                    // no effect
                    break;
            }
        }
    }
    public <T extends Document> boolean create(Collection<T> collection, List<UpdateOp> updateOps) {
        log("create", updateOps);
        List<T> docs = new ArrayList<T>();
        DBObject[] inserts = new DBObject[updateOps.size()];

        for (int i = 0; i < updateOps.size(); i++) {
            inserts[i] = new BasicDBObject();
            UpdateOp update = updateOps.get(i);
            T target = collection.newDocument(this);
            UpdateUtils.applyChanges(target, update, comparator);
            docs.add(target);
            for (Entry<Key, Operation> entry : update.getChanges().entrySet()) {
                Key k = entry.getKey();
                Operation op = entry.getValue();
                switch (op.type) {
                    case SET:
                    case MAX:
                    case INCREMENT: {
                        inserts[i].put(k.toString(), op.value);
                        break;
                    }
                    case SET_MAP_ENTRY: {
                        Revision r = k.getRevision();
                        if (r == null) {
                            throw new IllegalStateException(
                                    "SET_MAP_ENTRY must not have null revision");
                        }
                        DBObject value = new RevisionEntry(r, op.value);
                        inserts[i].put(k.getName(), value);
                        break;
                    }
                    case REMOVE_MAP_ENTRY:
                        // nothing to do for new entries
                        break;
                    case CONTAINS_MAP_ENTRY:
                        // no effect
                        break;
                }
            }
            if (!inserts[i].containsField(Document.MOD_COUNT)) {
                inserts[i].put(Document.MOD_COUNT, 1L);
                target.put(Document.MOD_COUNT, 1L);
            }
        }

        DBCollection dbCollection = getDBCollection(collection);
        long start = start();
        try {
            try {
                WriteResult writeResult = dbCollection.insert(inserts, WriteConcern.SAFE);
                if (writeResult.getError() != null) {
                    return false;
                }
                if (collection == Collection.NODES) {
                    for (T doc : docs) {
                        Lock lock = getAndLock(doc.getId());
                        try {
                            addToCache((NodeDocument) doc);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
                return true;
            } catch (MongoException e) {
                return false;
            }
        } finally {
            end("create", start);
        }
    }
    private static DBObject createUpdate(UpdateOp updateOp) {
        BasicDBObject setUpdates = new BasicDBObject();
        BasicDBObject maxUpdates = new BasicDBObject();
        BasicDBObject incUpdates = new BasicDBObject();
        BasicDBObject unsetUpdates = new BasicDBObject();

        // always increment modCount
        updateOp.increment(Document.MOD_COUNT, 1);

        // other updates
        for (Entry<Key, Operation> entry : updateOp.getChanges().entrySet()) {
            Key k = entry.getKey();
            if (k.getName().equals(Document.ID)) {
                // avoid exception "Mod on _id not allowed"
                continue;
            }
            Operation op = entry.getValue();
            switch (op.type) {
                case SET:
                case SET_MAP_ENTRY: {
                    setUpdates.append(k.toString(), op.value);
                    break;
                }
                case MAX: {
                    maxUpdates.append(k.toString(), op.value);
                    break;
                }
                case INCREMENT: {
                    incUpdates.append(k.toString(), op.value);
                    break;
                }
                case REMOVE_MAP_ENTRY: {
                    unsetUpdates.append(k.toString(), "1");
                    break;
                }
            }
        }

        BasicDBObject update = new BasicDBObject();
        if (!setUpdates.isEmpty()) {
            update.append("$set", setUpdates);
        }
        if (!maxUpdates.isEmpty()) {
            update.append("$max", maxUpdates);
        }
        if (!incUpdates.isEmpty()) {
            update.append("$inc", incUpdates);
        }
        if (!unsetUpdates.isEmpty()) {
            update.append("$unset", unsetUpdates);
        }

        return update;
    }
