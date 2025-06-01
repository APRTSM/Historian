    public boolean isModified() {
        if (writeState == null) {
            return false;
        }
        else {
            NodeState baseState = getBaseState();
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
    public NodeState getBaseState() {
        return baseState;
    }
