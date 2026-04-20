    public Set<String> find(String name, PropertyValue value) {
        Set<String> paths = Sets.newHashSet();

        NodeState state = getIndexDefinitionNode(name);
        if (state != null && state.getChildNode(":index") != null) {
            state = state.getChildNode(":index");
            if (value == null) {
                paths.addAll(store.find(state, null));
            } else {
                paths.addAll(store.find(state, Property2Index.encode(value)));
            }
        } else {
            // No index available, so first check this node for a match
            PropertyState property = root.getProperty(name);
            if (property != null) {
                if (value == null || value.isArray()) {
                    // let query engine handle property existence and
                    // multi-valued look ups;
                    // simply return all nodes that have this property
                    paths.add("");
                } else {
                    // does it match any of the values of this property?
                    for (int i = 0; i < property.count(); i++) {
                        if (property.getValue(value.getType(), i).equals(value.getValue(value.getType()))) {
                            paths.add("");
                            // no need to check for more matches in this property
                            break;
                        }
                    }
                }
            }

            // ... and then recursively look up from the rest of the tree
            for (ChildNodeEntry entry : root.getChildNodeEntries()) {
                String base = entry.getName();
                Property2IndexLookup lookup =
                        new Property2IndexLookup(entry.getNodeState());
                for (String path : lookup.find(name, value)) {
                    if (path.isEmpty()) {
                        paths.add(base);
                    } else {
                        paths.add(base + "/" + path);
                    }
                }
            }
        }

        return paths;
    }
    private NodeState getIndexDefinitionNode(String name) {
        NodeState state = root.getChildNode(INDEX_DEFINITIONS_NAME);
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
    public boolean isIndexed(String name, String path) {
        if (getIndexDefinitionNode(name) != null) {
            return true;
        }

        // TODO use PathUtils
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        int slash = path.indexOf('/');
        if (slash == -1) {
            return false;
        }

        NodeState child = root.getChildNode(path.substring(0, slash));
        return new Property2IndexLookup(child).isIndexed(
                name, path.substring(slash));
    }
    public double getCost(String name, PropertyValue value) {
        double cost = 0.0;
        // TODO the cost method is currently reading all the data - 
        // is not supposed to do that, it is only supposed to estimate
        NodeState state = getIndexDefinitionNode(name);
        if (state != null && state.getChildNode(":index") != null) {
            state = state.getChildNode(":index");
            if (value == null) {
                cost += store.count(state, null);
            } else {
                cost += store.count(state, Property2Index.encode(value));
            }
        } else {
            cost = Double.POSITIVE_INFINITY;
        }
        return cost;
    }
