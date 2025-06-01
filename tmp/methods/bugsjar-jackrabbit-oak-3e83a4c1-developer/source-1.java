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
    void setMapEntry(@Nonnull String property, @Nonnull Revision revision, String value) {
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
