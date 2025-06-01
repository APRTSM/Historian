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
    void setMapEntry(@Nonnull String property, @Nonnull Revision revision, String value) {
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
