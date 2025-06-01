    public static void setModified(@Nonnull UpdateOp op,
                                   @Nonnull Revision revision) {
        checkNotNull(op).set(MODIFIED_IN_SECS, getModifiedInSecs(checkNotNull(revision).getTimestamp()));
    }
    public void increment(@Nonnull String property, long value) {
        Operation op = new Operation();
        op.type = Operation.Type.INCREMENT;
        op.value = value;
        changes.put(new Key(property, null), op);
    }
    public void removeMapEntry(@Nonnull String property, @Nonnull Revision revision) {
        Operation op = new Operation();
        op.type = Operation.Type.REMOVE_MAP_ENTRY;
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
    void containsMapEntry(@Nonnull String property,
                          @Nonnull Revision revision,
                          boolean exists) {
        if (isNew) {
            throw new IllegalStateException("Cannot use containsMapEntry() on new document");
        }
        Operation op = new Operation();
        op.type = Operation.Type.CONTAINS_MAP_ENTRY;
        op.value = exists;
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
    void set(String property, Object value) {
        Operation op = new Operation();
        op.type = Operation.Type.SET;
        op.value = value;
        changes.put(new Key(property, null), op);
    }
    void setMapEntry(@Nonnull String property, @Nonnull Revision revision, Object value) {
        Operation op = new Operation();
        op.type = Operation.Type.SET_MAP_ENTRY;
        op.value = value;
        changes.put(new Key(property, checkNotNull(revision)), op);
    }
        public Operation getReverse() {
            Operation reverse = null;
            switch (type) {
            case INCREMENT:
                reverse = new Operation();
                reverse.type = Type.INCREMENT;
                reverse.value = -(Long) value;
                break;
            case SET:
            case REMOVE_MAP_ENTRY:
            case CONTAINS_MAP_ENTRY:
                // nothing to do
                break;
            case SET_MAP_ENTRY:
                reverse = new Operation();
                reverse.type = Type.REMOVE_MAP_ENTRY;
                break;
            }
            return reverse;
        }
    public static void applyChanges(@Nonnull Document doc, @Nonnull UpdateOp update, @Nonnull Comparator<Revision> comparator) {
        for (Entry<Key, Operation> e : checkNotNull(update).getChanges().entrySet()) {
            Key k = e.getKey();
            Operation op = e.getValue();
            switch (op.type) {
                case SET: {
                    doc.put(k.toString(), op.value);
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
