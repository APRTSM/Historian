    public TreeLocation getChild(String name) {
        PropertyState prop = getPropertyState(name);
        if (prop != null) {
            return createPropertyLocation(this, name);
        }

        T child = getChildTree(name);
        if (child != null) {
            return createNodeLocation(child);
        }
        return new NullLocation(this, name);
    }
    private TreeImpl internalGetChild(String childName) {
        return new TreeImpl(root, this, childName, pendingMoves);
    }
    TreeImpl getTree(String path) {
        checkArgument(PathUtils.isAbsolute(path));
        TreeImpl child = this;
        for (String name : elements(path)) {
            child = child.internalGetChild(name);
        }
        return canRead(child) ? child : null;
    }
    private TreeImpl(RootImpl root, TreeImpl parent, String name, Move pendingMoves) {
        this.root = checkNotNull(root);
        this.parent = checkNotNull(parent);
        this.name = checkNotNull(name);
        this.nodeBuilder = parent.nodeBuilder.getChildNode(name);
        this.pendingMoves = checkNotNull(pendingMoves);
    }
    public TreeImpl getChild(@Nonnull String name) {
        checkNotNull(name);
        enter();
        TreeImpl child = internalGetChild(name);
        return canRead(child) ? child : null;
    }
