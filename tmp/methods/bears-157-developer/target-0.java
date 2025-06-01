    public boolean isSameNode(@Nonnull String nodeString) {
        NodeLocator otherNode = NodeLocator.parseString(nodeString);
        // The nodes are the same if their Node IDs are the same.
        if (otherNode.getNodeId() != null && otherNode.getNodeId().equals(getNodeId())) {
            return true;
        } else {
            // Otherwise, the both node IDs must not be set
            // and must match by host and port.
            return !(otherNode.getNodeId() == null && getNodeId() != null)
                && otherNode.getHost().equals(getHost())
                && otherNode.getPort() == getPort();
        }
    }
