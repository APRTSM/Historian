    public static String getId(NodeStore store) {
        NodeState state = store.getRoot().getChildNode(CLUSTER_CONFIG_NODE);
        if (state.hasProperty(CLUSTER_ID_PROP)) {
            return state.getProperty(CLUSTER_ID_PROP).getValue(Type.STRING);
        }
        return null;
    }
