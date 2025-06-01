    public NodeState getChildNode(@Nonnull String name) {
        return getChildNode(name, lastRevision);
    }
    NodeState getChildNode(@Nonnull String name,
                           @Nonnull Revision revision) {
        if (!hasChildren) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        }
        String p = PathUtils.concat(getPath(), name);
        DocumentNodeState child = store.getNode(p, checkNotNull(revision));
        if (child == null) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        } else {
            return child;
        }
    }
