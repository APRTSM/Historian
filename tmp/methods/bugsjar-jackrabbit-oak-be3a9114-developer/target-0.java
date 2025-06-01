    public boolean propertyChanged(PropertyState before, PropertyState after) {
        if (!loader.isRunning()) {
            return false;
        }
        if (!logOnly) {
            builder.setProperty(binaryCheck(after));
        }
        return true;
    }
    public boolean childNodeAdded(String name, NodeState after) {
        if (!loader.isRunning()) {
            return false;
        }

        if (after instanceof SegmentNodeState) {
            if (log.isTraceEnabled()) {
                log.trace("childNodeAdded {}, RO:{}", path + name, logOnly);
            }
            if (!logOnly) {
                RecordId id = ((SegmentNodeState) after).getRecordId();
                builder.setChildNode(name, new SegmentNodeState(id));
            }
            return true;
        }
        return false;
    }
    public boolean propertyAdded(PropertyState after) {
        if (!loader.isRunning()) {
            return false;
        }
        if (!logOnly) {
            builder.setProperty(binaryCheck(after));
        }
        return true;
    }
    public StandbyApplyDiff(NodeBuilder builder, SegmentStore store,
            RemoteSegmentLoader loader) {
        this(builder, store, loader, "/", false);
    }
    public boolean childNodeChanged(String name, NodeState before,
            NodeState after) {
        if (!loader.isRunning()) {
            return false;
        }

        if (after instanceof SegmentNodeState) {
            RecordId id = ((SegmentNodeState) after).getRecordId();

            if (log.isTraceEnabled()) {
                // if (PathUtils.getDepth(path) < 5) {
                RecordId oldId = ((SegmentNodeState) before).getRecordId();
                log.trace("childNodeChanged {}, {} -> {}, RO:{}", path + name,
                        oldId, id, logOnly);
                // }
            }
            if (!logOnly) {
                builder.setChildNode(name, new SegmentNodeState(id));
            }

            // return true;
            return after.compareAgainstBaseState(before, new StandbyApplyDiff(
                    builder.getChildNode(name), store, loader, path + name
                            + "/", true));
        }
        return false;
    }
    private StandbyApplyDiff(NodeBuilder builder, SegmentStore store,
            RemoteSegmentLoader loader, String path, boolean logOnly) {
        this.builder = builder;
        this.store = store;
        this.loader = loader;
        this.path = path;
        this.logOnly = logOnly;
    }
    public boolean propertyDeleted(PropertyState before) {
        if (!loader.isRunning()) {
            return false;
        }
        if (!logOnly) {
            builder.removeProperty(before.getName());
        }
        return true;
    }
    public boolean childNodeDeleted(String name, NodeState before) {
        if (!loader.isRunning()) {
            return false;
        }
        log.trace("childNodeDeleted {}, RO:{}", path + name, logOnly);
        if (!logOnly) {
            builder.getChildNode(name).remove();
        }
        return true;
    }
