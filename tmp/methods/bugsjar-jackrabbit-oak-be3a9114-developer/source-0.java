    public boolean propertyAdded(PropertyState after) {
        if (!loader.isRunning()) {
            return false;
        }
        builder.setProperty(binaryCheck(after));
        return true;
    }
    private StandbyApplyDiff(NodeBuilder builder, SegmentStore store,
            RemoteSegmentLoader loader, String path) {
        this.builder = builder;
        this.store = store;
        this.loader = loader;
        this.path = path;
        if (log.isTraceEnabled()) {
            if (PathUtils.getDepth(path) < 5) {
                log.trace("running diff on {}", path);
            }
        }
    }
    public boolean childNodeAdded(String name, NodeState after) {
        if (!loader.isRunning()) {
            return false;
        }
        NodeBuilder child = EmptyNodeState.EMPTY_NODE.builder();
        boolean success = EmptyNodeState.compareAgainstEmptyState(after,
                new StandbyApplyDiff(child, store, loader, path + name + "/"));
        if (success) {
            builder.setChildNode(name, child.getNodeState());
        }
        return success;
    }
    public StandbyApplyDiff(NodeBuilder builder, SegmentStore store,
            RemoteSegmentLoader loader) {
        this(builder, store, loader, "/");
    }
    public boolean propertyChanged(PropertyState before, PropertyState after) {
        if (!loader.isRunning()) {
            return false;
        }
        builder.setProperty(binaryCheck(after));
        return true;
    }
    public boolean childNodeChanged(String name, NodeState before,
            NodeState after) {
        if (!loader.isRunning()) {
            return false;
        }

        return after.compareAgainstBaseState(before, new StandbyApplyDiff(
                builder.getChildNode(name), store, loader, path + name + "/"));
    }
    public boolean propertyDeleted(PropertyState before) {
        if (!loader.isRunning()) {
            return false;
        }
        builder.removeProperty(before.getName());
        return true;
    }
    public boolean childNodeDeleted(String name, NodeState before) {
        if (!loader.isRunning()) {
            return false;
        }
        builder.getChildNode(name).remove();
        return true;
    }
