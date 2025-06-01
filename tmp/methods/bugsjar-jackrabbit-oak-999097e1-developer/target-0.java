    public NodeState getBaseState() {
        read();
        return baseState;
    }
    public boolean isModified() {
        NodeState baseState = getBaseState();
        if (writeState == null) {
            return false;
        }
        else {
            Map<String, MutableNodeState> nodes = writeState.nodes;
            for (Entry<String, MutableNodeState> n : writeState.nodes.entrySet()) {
                if (n.getValue() == null) {
                    return true;
                }
                if (baseState == null || !baseState.hasChildNode(n.getKey())) {
                    return true;
                }
            }
            for (Entry<String, PropertyState> p : writeState.properties.entrySet()) {
                PropertyState pState = p.getValue();
                if (pState == null) {
                    return true;
                }
                if (baseState == null || !pState.equals(baseState.getProperty(p.getKey()))) {
                    return true;
                }
            }
            return false;
        }
    }
