    public TreeLocation getChild(String name) {
        T child = getChildTree(name);
        if (child != null) {
            return createNodeLocation(child);
        }

        PropertyState prop = getPropertyState(name);
        if (prop != null) {
            return createPropertyLocation(this, name);
        }
        return new NullLocation(this, name);
    }
    public TreeImpl getChild(@Nonnull String name) {
        checkNotNull(name);
        enter();
        TreeImpl child = internalGetChild(name);
        if (child != null && canRead(child)) {
            return child;
        } else {
            return null;
        }
    }
    private TreeImpl internalGetChild(String childName) {
        return nodeBuilder.hasChildNode(childName)
            ? new TreeImpl(root, this, childName, pendingMoves)
            : null;
    }
    private TreeImpl(RootImpl root, TreeImpl parent, String name, Move pendingMoves) {
        this.root = checkNotNull(root);
        this.parent = checkNotNull(parent);
        this.name = checkNotNull(name);
        this.nodeBuilder = parent.nodeBuilder.child(name);
        this.pendingMoves = checkNotNull(pendingMoves);
    }
    TreeImpl getTree(String path) {
        checkArgument(PathUtils.isAbsolute(path));
        TreeImpl child = this;
        for (String name : elements(path)) {
            child = child.internalGetChild(name);
            if (child == null) {
                return null;
            }
        }
        return (canRead(child)) ? child : null;
    }
