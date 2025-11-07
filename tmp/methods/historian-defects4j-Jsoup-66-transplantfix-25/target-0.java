    protected List<Node> ensureChildNodes() {
shadowChildrenRef = null;
        if (childNodes == EMPTY_NODES) {
            childNodes = new NodeList(4);
        }
        return childNodes;
    }
