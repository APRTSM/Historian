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
    public PropertyState setProperty(String name, List<CoreValue> values) {
        PropertyState property = new PropertyStateImpl(name, values);
        if (hasProperty(name)) {
            context.setProperty(property, path);
        }
        else {
            context.addProperty(property, path);
        }
        return property;
    }
    public static NodeStateBuilder create(NodeStateBuilderContext context) {
        return new KernelNodeStateBuilder(context, "");
    }
    public boolean removeNode(String name) {
        if (hasChild(name)) {
            context.removeNode(PathUtils.concat(path, name));
            return true;
        }
        else {
            return false;
        }
    }
    public NodeStateBuilder getChildBuilder(String name) {
        return hasChild(name)
            ? new KernelNodeStateBuilder(context, PathUtils.concat(path, name))
            : null;
    }
    public NodeState getNodeState() {
        return context.getNodeState(path);
    }
    public PropertyState setProperty(String name, CoreValue value) {
        PropertyState property = new PropertyStateImpl(name, value);
        if (hasProperty(name)) {
            context.setProperty(property, path);
        }
        else {
            context.addProperty(property, path);
        }
        return property;
    }
    private KernelNodeStateBuilder(NodeStateBuilderContext context, String path) {
        this.context = context;
        this.path = path;
    }
    public NodeStateBuilder addNode(String name) {
        if (hasChild(name)) {
            return null;
        }
        else {
            String targetPath = PathUtils.concat(path, name);
            context.addNode(targetPath);
            return new KernelNodeStateBuilder(context, targetPath);
        }
    }
    public boolean copyTo(NodeStateBuilder destParent, String destName) {
        if (!(destParent instanceof KernelNodeStateBuilder)) {
            throw new IllegalArgumentException("Alien builder for destParent");
        }

        if (destParent.getChildBuilder(destName) != null) {
            return false;
        }

        KernelNodeStateBuilder destParentBuilder = (KernelNodeStateBuilder) destParent;
        String destPath = PathUtils.concat(destParentBuilder.path, destName);

        context.copyNode(path, destPath);
        return true;
    }
    public NodeStateBuilder addNode(String name, NodeState nodeState) {
        if (hasChild(name)) {
            return null;
        }
        else {
            String targetPath = PathUtils.concat(path, name);
            context.addNode(nodeState, targetPath);
            return new KernelNodeStateBuilder(context, targetPath);
        }
    }
    public void removeProperty(String name) {
        if (hasProperty(name)) {
            context.removeProperty(PathUtils.concat(path, name));
        }
    }
    public boolean moveTo(NodeStateBuilder destParent, String destName) {
        if (!(destParent instanceof KernelNodeStateBuilder)) {
            throw new IllegalArgumentException("Alien builder for destParent");
        }

        if (destParent.getChildBuilder(destName) != null) {
            return false;
        }

        KernelNodeStateBuilder destParentBuilder = (KernelNodeStateBuilder) destParent;
        String destPath = PathUtils.concat(destParentBuilder.path, destName);

        context.moveNode(path, destPath);
        path = destPath;
        return true;
    }
