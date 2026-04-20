    public static String getId(NodeStore store) {
        return store.getRoot().getChildNode(CLUSTER_CONFIG_NODE).getProperty(CLUSTER_ID_PROP).getValue(Type.STRING);
    }
