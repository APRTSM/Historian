    public boolean isSameNode(@Nonnull String nodeString) {
        NodeLocator otherNode = NodeLocator.parseString(nodeString);
        // The nodes are the same if their Node IDs are the same.
        if (otherNode.getNodeId() != null && otherNode.getNodeId().equals(getNodeId())) {
            return true;
        } else {
            // Otherwise, the other node must not have a node ID set
            // and must match by host and port.
            return otherNode.getNodeId() == null
                && otherNode.getHost().equals(getHost())
                && otherNode.getPort() == getPort();
        }
    }
