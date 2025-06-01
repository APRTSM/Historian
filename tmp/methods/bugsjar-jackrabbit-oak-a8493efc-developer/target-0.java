    public boolean isIndexed(String name, String path) {
        return isIndexed(root, name, path);
    }
    public double getCost(String name, PropertyValue value) {
        // TODO the cost method is currently reading all the data - 
        // is not supposed to do that, it is only supposed to estimate
        NodeState state = getIndexDefinitionNode(root, name);
        if (state == null || state.getChildNode(":index") == null) {
            return Double.POSITIVE_INFINITY;
        }
        state = state.getChildNode(":index");
        double cost;
        if (value == null) {
            cost = store.count(state, null);
        } else {
            cost = store.count(state, Property2Index.encode(value));
        }
        return cost;
    }
    private static NodeState getIndexDefinitionNode(NodeState node, String name) {
        NodeState state = node.getChildNode(INDEX_DEFINITIONS_NAME);
        if (state != null) {
            for (ChildNodeEntry entry : state.getChildNodeEntries()) {
                PropertyState type = entry.getNodeState().getProperty(IndexConstants.TYPE_PROPERTY_NAME);
                if (type == null || type.isArray() || !Property2Index.TYPE.equals(type.getValue(Type.STRING))) {
                    continue;
                }
                PropertyState names = entry.getNodeState().getProperty("propertyNames");
                if (names != null) {
                    for (int i = 0; i < names.count(); i++) {
                        if (name.equals(names.getValue(Type.STRING, i))) {
                            return entry.getNodeState();
                        }
                    }
                }
            }
        }
        return null;
    }
    public Set<String> find(String name, PropertyValue value) {
        NodeState state = getIndexDefinitionNode(root, name);
        if (state == null || state.getChildNode(":index") == null) {
            throw new IllegalArgumentException("No index for " + name);
        }
        Set<String> paths = Sets.newHashSet();
        state = state.getChildNode(":index");
        if (value == null) {
            paths.addAll(store.find(state, null));
        } else {
            paths.addAll(store.find(state, Property2Index.encode(value)));
        }
        return paths;
    }
    private static boolean isIndexed(NodeState root, String name, String path) {
        NodeState node = root;
        Iterator<String> it = PathUtils.elements(path).iterator();
        while (true) {
            if (getIndexDefinitionNode(node, name) != null) {
                return true;
            }
            if (!it.hasNext()) {
                break;
            }
            node = node.getChildNode(it.next());
        }
        return false;
    }
