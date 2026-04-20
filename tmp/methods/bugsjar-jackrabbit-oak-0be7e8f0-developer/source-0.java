    public boolean removeChild(String name) {
        boolean result = builder.removeNode(name);
        if (result) {
            listener.removeChild(this, name);
        }
        return result;
    }
    TreeImpl(NodeStore store, NodeStateBuilder nodeStateBuilder, Listener listener) {
        this(store, nodeStateBuilder.getNodeState(), nodeStateBuilder, null, "", listener);
    }
    public Iterable<Tree> getChildren() {
        return new Iterable<Tree>() {
            @Override
            public Iterator<Tree> iterator() {
                Iterator<? extends ChildNodeEntry> childEntries =
                        getNodeState().getChildNodeEntries().iterator();

                return Iterators.map(childEntries, new Function1<ChildNodeEntry, Tree>() {
                    @Override
                    public Tree apply(ChildNodeEntry entry) {
                        NodeStateBuilder childBuilder = builder.getChildBuilder(entry.getName());
                        return new TreeImpl(store, childBuilder.getNodeState(), childBuilder, TreeImpl.this, entry.getName(), listener);
                    }
                });
            }
        };
    }
    public PropertyState setProperty(String name, CoreValue value) {
        PropertyState property = builder.setProperty(name, value);
        if (listener != null) {
            listener.setProperty(this, name, value);
        }
        return property;
    }
    private TreeImpl(NodeStore store, NodeState baseState, NodeStateBuilder builder,
            TreeImpl parent, String name, Listener listener) {

        this.store = store;
        this.builder = builder;
        this.baseState = baseState;
        this.listener = listener;
        this.name = name;
        this.parent = parent;
    }
    public PropertyState setProperty(String name, List<CoreValue> values) {
        PropertyState property = builder.setProperty(name, values);
        if (listener != null) {
            listener.setProperty(this, name, values);
        }
        return property;
    }
    public boolean move(TreeImpl destParent, String destName) {
        boolean result = builder.moveTo(destParent.builder, destName);
        if (result) {
            TreeImpl oldParent = parent;
            String oldName = name;

            name = destName;
            parent = destParent;

            if (listener != null) {
                listener.move(oldParent, oldName, this);
            }
        }
        return result;
    }
    public String getPath() {
        if (parent == null) {
            return name;
        }
        else {
            String path = parent.getPath();
            return path.isEmpty()
                    ? name
                    : path + '/' + name;
        }
    }
    public boolean copy(TreeImpl destParent, String destName) {
        boolean result = builder.copyTo(destParent.builder, destName);
        if (result) {
            if (listener != null) {
                listener.copy(parent, name, destParent.getChild(destName));
            }
            return true;
        }
        return result;
    }
    private NodeState getNodeState() {
        return builder.getNodeState();
    }
    public TreeImpl getChild(String name) {
        NodeStateBuilder childBuilder = builder.getChildBuilder(name);
        if (childBuilder == null) {
            return null;
        }
        else {
            NodeState childBaseState = baseState == null
                    ? null
                    : baseState.getChildNode(name);

            return new TreeImpl(store, childBaseState, childBuilder, this, name, listener);
        }
    }
    public void removeProperty(String name) {
        builder.removeProperty(name);
        if (listener != null) {
            listener.removeProperty(this, name);
        }
    }
    public Tree addChild(String name) {
        if (builder.addNode(name) != null) {
            listener.addChild(this, name);
        }
        return getChild(name);
    }
