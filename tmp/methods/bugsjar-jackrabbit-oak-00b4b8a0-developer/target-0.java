    NodeState getNodeState() {
        return nodeBuilder.getNodeState();
    }
    public Iterable<Tree> getChildren() {
        root.checkLive();
        Iterable<String> childNames;
        if (hasOrderableChildren()) {
            childNames = getOrderedChildNames();
        } else {
            childNames = nodeBuilder.getChildNodeNames();
        }
        return Iterables.filter(Iterables.transform(
                childNames,
                new Function<String, Tree>() {
                    @Override
                    public Tree apply(String input) {
                        return new TreeImpl(root, TreeImpl.this, input);
                    }
                }),
                new Predicate<Tree>() {
                    @Override
                    public boolean apply(Tree tree) {
                        return tree != null && canRead(tree);
                    }
                });
    }
    public <T> void setProperty(String name, T value, Type<T> type) {
        root.checkLive();
        NodeBuilder builder = nodeBuilder;
        builder.setProperty(name, value, type);
        root.updated();
    }
    private PropertyState internalGetProperty(String propertyName) {
        return nodeBuilder.getProperty(propertyName);
    }
    private TreeImpl internalGetChild(String childName) {
        return nodeBuilder.hasChildNode(childName)
                ? new TreeImpl(root, this, childName)
                : null;
    }
    void updateChildOrder() {
        if (!hasOrderableChildren()) {
            return;
        }
        Set<String> names = Sets.newLinkedHashSet();
        for (String name : getOrderedChildNames()) {
            if (nodeBuilder.hasChildNode(name)) {
                names.add(name);
            }
        }
        for (String name : nodeBuilder.getChildNodeNames()) {
            names.add(name);
        }
        PropertyBuilder<String> builder = MemoryPropertyBuilder.array(
                Type.STRING, OAK_CHILD_ORDER);
        builder.setValues(names);
        nodeBuilder.setProperty(builder.getPropertyState());
    }
    NodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }
    public long getChildrenCount() {
        // TODO: make sure cnt respects access control
        root.checkLive();
        return nodeBuilder.getChildNodeCount();
    }
    public boolean remove() {
        root.checkLive();
        if (isDisconnected()) {
            throw new IllegalStateException("Cannot remove a disconnected tree");
        }

        if (!isRoot() && parent.hasChild(name)) {
            NodeBuilder builder = parent.nodeBuilder;
            builder.removeNode(name);
            if (parent.hasOrderableChildren()) {
                builder.setProperty(
                        MemoryPropertyBuilder.copy(Type.STRING, parent.internalGetProperty(OAK_CHILD_ORDER))
                                .removeValue(name)
                                .getPropertyState()
                );
            }
            root.updated();
            return true;
        } else {
            return false;
        }
    }
    public <T> void setProperty(String name, T value) {
        root.checkLive();
        NodeBuilder builder = nodeBuilder;
        builder.setProperty(name, value);
        root.updated();
    }
    private TreeImpl(RootImpl root, TreeImpl parent, String name) {
        this.root = checkNotNull(root);
        this.parent = checkNotNull(parent);
        this.name = checkNotNull(name);
        this.nodeBuilder = parent.getNodeBuilder().child(name);
    }
    private TreeImpl(RootImpl root) {
        this.root = checkNotNull(root);
        this.name = "";
        this.nodeBuilder = root.createRootBuilder();
    }
    public void ensureChildOrderProperty() {
        PropertyState childOrder = nodeBuilder.getProperty(OAK_CHILD_ORDER);
        if (childOrder == null) {
            nodeBuilder.setProperty(
                    MultiStringPropertyState.stringProperty(OAK_CHILD_ORDER, nodeBuilder.getChildNodeNames()));
        }
    }
    public void setProperty(PropertyState property) {
        root.checkLive();
        NodeBuilder builder = nodeBuilder;
        builder.setProperty(property);
        root.updated();
    }
    static TreeImpl createRoot(final RootImpl root) {
        return new TreeImpl(root) {
            @Override
            protected NodeState getBaseState() {
                return root.getBaseState();
            }
        };
    }
    public Status getStatus() {
        root.checkLive();

        if (isDisconnected()) {
            return Status.DISCONNECTED;
        }

        NodeBuilder builder = nodeBuilder;
        if (builder.isNew()) {
            return Status.NEW;
        } else if (builder.isModified()) {
            return Status.MODIFIED;
        } else {
            return Status.EXISTING;
        }
    }
    public Iterable<? extends PropertyState> getProperties() {
        root.checkLive();
        return Iterables.filter(nodeBuilder.getProperties(),
                new Predicate<PropertyState>() {
                    @Override
                    public boolean apply(PropertyState propertyState) {
                        return canRead(propertyState);
                    }
                });
    }
    public boolean orderBefore(final String name) {
        root.checkLive();
        if (isRoot()) {
            // root does not have siblings
            return false;
        }
        if (name != null && !parent.hasChild(name)) {
            // so such sibling or not accessible
            return false;
        }
        // perform the reorder
        parent.ensureChildOrderProperty();
        // all siblings but not this one
        Iterable<String> filtered = Iterables.filter(
                parent.getOrderedChildNames(),
                new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return !TreeImpl.this.getName().equals(input);
                    }
                });
        // create head and tail
        Iterable<String> head;
        Iterable<String> tail;
        if (name == null) {
            head = filtered;
            tail = Collections.emptyList();
        } else {
            int idx = Iterables.indexOf(filtered, new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return name.equals(input);
                }
            });
            head = Iterables.limit(filtered, idx);
            tail = Iterables.skip(filtered, idx);
        }
        // concatenate head, this name and tail
        parent.nodeBuilder.setProperty(MultiStringPropertyState.stringProperty(OAK_CHILD_ORDER, Iterables.concat(head, Collections.singleton(getName()), tail))
        );
        root.updated();
        return true;
    }
    public void removeProperty(String name) {
        root.checkLive();
        NodeBuilder builder = nodeBuilder;
        builder.removeProperty(name);
        root.updated();
    }
    public Tree addChild(String name) {
        root.checkLive();
        if (!hasChild(name)) {
            nodeBuilder.child(name);
            if (hasOrderableChildren()) {
                nodeBuilder.setProperty(
                        MemoryPropertyBuilder.copy(Type.STRING, internalGetProperty(OAK_CHILD_ORDER))
                                .addValue(name)
                                .getPropertyState());
            }
            root.updated();
        }

        TreeImpl child = new TreeImpl(root, this, name);

        // Make sure to allocate the node builder for new nodes in order to correctly
        // track removes and moves. See OAK-621
        return child;
    }
    private boolean isDisconnected() {
        if (isRoot()) {
            return false;
        }
        if (parent.nodeBuilder == null) {
            return false;
        }
        if (!parent.nodeBuilder.isConnected()) {
            return true;
        }
        return !nodeBuilder.isConnected();
    }
