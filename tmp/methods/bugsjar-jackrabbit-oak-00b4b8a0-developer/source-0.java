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
        return !getNodeBuilder().isConnected();
    }
    public void ensureChildOrderProperty() {
        PropertyState childOrder = getNodeBuilder().getProperty(OAK_CHILD_ORDER);
        if (childOrder == null) {
            getNodeBuilder().setProperty(
                    MultiStringPropertyState.stringProperty(OAK_CHILD_ORDER, getNodeBuilder().getChildNodeNames()));
        }
    }
    public <T> void setProperty(String name, T value) {
        root.checkLive();
        NodeBuilder builder = getNodeBuilder();
        builder.setProperty(name, value);
        root.updated();
    }
    public Iterable<Tree> getChildren() {
        root.checkLive();
        Iterable<String> childNames;
        if (hasOrderableChildren()) {
            childNames = getOrderedChildNames();
        } else {
            childNames = getNodeBuilder().getChildNodeNames();
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
    public boolean remove() {
        root.checkLive();
        if (isDisconnected()) {
            throw new IllegalStateException("Cannot remove a disconnected tree");
        }

        if (!isRoot() && parent.hasChild(name)) {
            NodeBuilder builder = parent.getNodeBuilder();
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
    static TreeImpl createRoot(final RootImpl root) {
        return new TreeImpl(root, null, "") {
            @Override
            protected NodeState getBaseState() {
                return root.getBaseState();
            }

            @Override
            protected synchronized NodeBuilder getNodeBuilder() {
                if (nodeBuilder == null) {
                    nodeBuilder = root.createRootBuilder();
                }
                return nodeBuilder;
            }
        };
    }
    public void setProperty(PropertyState property) {
        root.checkLive();
        NodeBuilder builder = getNodeBuilder();
        builder.setProperty(property);
        root.updated();
    }
    private TreeImpl(RootImpl root, TreeImpl parent, String name) {
        this.root = checkNotNull(root);
        this.parent = parent;
        this.name = checkNotNull(name);
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
        parent.getNodeBuilder().setProperty(MultiStringPropertyState.stringProperty(OAK_CHILD_ORDER, Iterables.concat(head, Collections.singleton(getName()), tail))
        );
        root.updated();
        return true;
    }
    NodeState getNodeState() {
        return getNodeBuilder().getNodeState();
    }
    private PropertyState internalGetProperty(String propertyName) {
        return getNodeBuilder().getProperty(propertyName);
    }
    public long getChildrenCount() {
        // TODO: make sure cnt respects access control
        root.checkLive();
        return getNodeBuilder().getChildNodeCount();
    }
    public Tree addChild(String name) {
        root.checkLive();
        if (!hasChild(name)) {
            getNodeBuilder().child(name);
            if (hasOrderableChildren()) {
                getNodeBuilder().setProperty(
                        MemoryPropertyBuilder.copy(Type.STRING, internalGetProperty(OAK_CHILD_ORDER))
                                .addValue(name)
                                .getPropertyState());
            }
            root.updated();
        }

        TreeImpl child = getChild(name);
        assert child != null;
        return child;
    }
    private TreeImpl internalGetChild(String childName) {
        return getNodeBuilder().hasChildNode(childName)
                ? new TreeImpl(root, this, childName)
                : null;
    }
    public void removeProperty(String name) {
        root.checkLive();
        NodeBuilder builder = getNodeBuilder();
        builder.removeProperty(name);
        root.updated();
    }
    public Status getStatus() {
        root.checkLive();

        if (isDisconnected()) {
            return Status.DISCONNECTED;
        }

        NodeBuilder builder = getNodeBuilder();
        if (builder.isNew()) {
            return Status.NEW;
        } else if (builder.isModified()) {
            return Status.MODIFIED;
        } else {
            return Status.EXISTING;
        }
    }
    protected synchronized NodeBuilder getNodeBuilder() {
        if (nodeBuilder == null) {
            nodeBuilder = parent.getNodeBuilder().child(name);
        }
        return nodeBuilder;
    }
    void updateChildOrder() {
        if (!hasOrderableChildren()) {
            return;
        }
        Set<String> names = Sets.newLinkedHashSet();
        for (String name : getOrderedChildNames()) {
            if (getNodeBuilder().hasChildNode(name)) {
                names.add(name);
            }
        }
        for (String name : getNodeBuilder().getChildNodeNames()) {
            names.add(name);
        }
        PropertyBuilder<String> builder = MemoryPropertyBuilder.array(
                Type.STRING, OAK_CHILD_ORDER);
        builder.setValues(names);
        getNodeBuilder().setProperty(builder.getPropertyState());
    }
    public Iterable<? extends PropertyState> getProperties() {
        root.checkLive();
        return Iterables.filter(getNodeBuilder().getProperties(),
                new Predicate<PropertyState>() {
                    @Override
                    public boolean apply(PropertyState propertyState) {
                        return canRead(propertyState);
                    }
                });
    }
    public <T> void setProperty(String name, T value, Type<T> type) {
        root.checkLive();
        NodeBuilder builder = getNodeBuilder();
        builder.setProperty(name, value, type);
        root.updated();
    }
