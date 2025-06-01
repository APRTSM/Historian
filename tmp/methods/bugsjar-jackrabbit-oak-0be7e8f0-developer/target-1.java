    public void removeProperty(String name) {
        getBuilder().removeProperty(name);
        if (listener != null) {
            listener.removeProperty(this, name);
        }
    }
    public boolean removeChild(String name) {
        boolean result = getBuilder().removeNode(name);
        if (result) {
            listener.removeChild(this, name);
            children.remove(name);
        }
        return result;
    }
    public boolean move(TreeImpl destParent, String destName) {
        NodeStateBuilder builder = getBuilder();
        NodeStateBuilder destParentBuilder = destParent.getBuilder();
        boolean result = builder.moveTo(destParentBuilder, destName);
        if (result) {
            parent.children.remove(name);
            destParent.children.put(destName, this);

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
    private NodeStateBuilder getBuilder() {
        NodeStateBuilder builder = rootBuilder;
        for (String name : PathUtils.elements(getPath())) {
            builder = builder.getChildBuilder(name);
            if (builder == null) {
                throw new IllegalStateException("Stale NodeStateBuilder for " + getPath());
            }
        }

        return builder;
    }
    private NodeState getNodeState() {
        return getBuilder().getNodeState();
    }
    private void buildPath(StringBuilder sb) {
        if (parent != null) {
            parent.buildPath(sb);
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(name);
        }
    }
    public Iterable<Tree> getChildren() {
        return new Iterable<Tree>() {
            @Override
            public Iterator<Tree> iterator() {
                final NodeState nodeState = getNodeState();

                Iterator<? extends ChildNodeEntry> childEntries =
                        nodeState.getChildNodeEntries().iterator();

                return Iterators.map(childEntries, new Function1<ChildNodeEntry, Tree>() {
                    @Override
                    public Tree apply(ChildNodeEntry entry) {
                        String childName = entry.getName();
                        TreeImpl child = children.get(entry.getName());
                        if (child != null) {
                            return child;
                        }

                        NodeState childNodeState = nodeState.getChildNode(childName);
                        child = new TreeImpl(store, childNodeState, rootBuilder, TreeImpl.this, childName, listener);
                        children.put(childName, child);
                        return child;
                    }
                });
            }
        };
    }
    TreeImpl(NodeStore store, NodeStateBuilder rootBuilder, Listener listener) {
        this(store, rootBuilder.getNodeState(), rootBuilder, null, "", listener);
    }
    public PropertyState setProperty(String name, CoreValue value) {
        PropertyState property = getBuilder().setProperty(name, value);
        if (listener != null) {
            listener.setProperty(this, name, value);
        }
        return property;
    }
    public Tree addChild(String name) {
        if (getBuilder().addNode(name) != null) {
            listener.addChild(this, name);
        }
        TreeImpl child = getChild(name);
        children.put(name, child);
        return child;
    }
    public String getPath() {
        // Shortcut for root
        if (parent == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        buildPath(sb);
        return sb.toString();
    }
    private TreeImpl(NodeStore store, NodeState baseState, NodeStateBuilder rootBuilder,
            TreeImpl parent, String name, Listener listener) {

        this.store = store;
        this.rootBuilder = rootBuilder;
        this.baseState = baseState;
        this.listener = listener;
        this.parent = parent;
        this.name = name;
    }
    public TreeImpl getChild(String name) {
        TreeImpl child = children.get(name);
        if (child != null) {
            return child;
        }

        if (!hasChild(name)) {
            return null;
        }

        NodeState childBaseState = baseState == null
                ? null
                : baseState.getChildNode(name);

        child = new TreeImpl(store, childBaseState, rootBuilder, this, name, listener);
        children.put(name, child);
        return child;
    }
    public PropertyState setProperty(String name, List<CoreValue> values) {
        PropertyState property = getBuilder().setProperty(name, values);
        if (listener != null) {
            listener.setProperty(this, name, values);
        }
        return property;
    }
    public boolean copy(TreeImpl destParent, String destName) {
        boolean result = getBuilder().copyTo(destParent.getBuilder(), destName);
        if (result) {
            if (listener != null) {
                listener.copy(parent, name, destParent.getChild(destName));
            }
            return true;
        }
        return result;
    }
    public NodeStateBuilder addNode(String name, NodeState nodeState) {
        if (hasChild(name)) {
            return null;
        }
        else {
            String targetPath = PathUtils.concat(getPath(), name);
            context.addNode(nodeState, targetPath);
            return new KernelNodeStateBuilder(context, this, name);
        }
    }
    public NodeState getNodeState() {
        return context.getNodeState(getPath());
    }
    public boolean moveTo(NodeStateBuilder destParent, String destName) {
        if (!(destParent instanceof KernelNodeStateBuilder)) {
            throw new IllegalArgumentException("Alien builder for destParent");
        }

        if (destParent.getChildBuilder(destName) != null) {
            return false;
        }

        KernelNodeStateBuilder destParentBuilder = (KernelNodeStateBuilder) destParent;
        String destPath = PathUtils.concat(destParentBuilder.getPath(), destName);

        context.moveNode(getPath(), destPath);

        name = destName;
        parent = destParentBuilder;

        return true;
    }
    public boolean removeNode(String name) {
        if (hasChild(name)) {
            context.removeNode(PathUtils.concat(getPath(), name));
            return true;
        }
        else {
            return false;
        }
    }
    public void removeProperty(String name) {
        if (hasProperty(name)) {
            context.removeProperty(PathUtils.concat(getPath(), name));
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
        String destPath = PathUtils.concat(destParentBuilder.getPath(), destName);

        context.copyNode(getPath(), destPath);
        return true;
    }
    public NodeStateBuilder addNode(String name) {
        if (hasChild(name)) {
            return null;
        }
        else {
            String targetPath = PathUtils.concat(getPath(), name);
            context.addNode(targetPath);
            return new KernelNodeStateBuilder(context, this, name);
        }
    }
    public PropertyState setProperty(String name, CoreValue value) {
        PropertyState property = new PropertyStateImpl(name, value);
        if (hasProperty(name)) {
            context.setProperty(property, getPath());
        }
        else {
            context.addProperty(property, getPath());
        }
        return property;
    }
    private void buildPath(StringBuilder sb) {
        if (parent != null) {
            parent.buildPath(sb);
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(name);
        }
    }
    private KernelNodeStateBuilder(NodeStateBuilderContext context, KernelNodeStateBuilder parent, String name) {
        this.context = context;
        this.parent = parent;
        this.name = name;
    }
    public PropertyState setProperty(String name, List<CoreValue> values) {
        PropertyState property = new PropertyStateImpl(name, values);
        if (hasProperty(name)) {
            context.setProperty(property, getPath());
        }
        else {
            context.addProperty(property, getPath());
        }
        return property;
    }
    private String getPath() {
        // Shortcut for root
        if (parent == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        buildPath(sb);
        return sb.toString();
    }
    public NodeStateBuilder getChildBuilder(String name) {
        return hasChild(name)
            ? new KernelNodeStateBuilder(context, this, name)
            : null;
    }
    public static NodeStateBuilder create(NodeStateBuilderContext context) {
        return new KernelNodeStateBuilder(context, null, "");
    }
