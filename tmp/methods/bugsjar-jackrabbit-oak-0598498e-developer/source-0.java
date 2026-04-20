    public NodeState getChildNode(@Nonnull String name) {
        if (!hasChildren) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        }
        String p = PathUtils.concat(getPath(), name);
        DocumentNodeState child = store.getNode(p, lastRevision);
        if (child == null) {
            checkValidName(name);
            return EmptyNodeState.MISSING_NODE;
        } else {
            return child;
        }
    }
