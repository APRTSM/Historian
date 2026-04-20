    public boolean equals(Object object) {
        if (object == EMPTY_NODE || object == MISSING_NODE) {
            return exists == (object == EMPTY_NODE);
        } else if (object instanceof NodeState) {
            NodeState that = (NodeState) object;
            return that.getPropertyCount() == 0
                    && that.getChildNodeCount(1) == 0
                    && (exists == that.exists());
        } else {
            return false;
        }
    }
