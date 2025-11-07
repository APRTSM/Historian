    public Node before(Node node) {
        int siblingIndex = childNodeSize();
Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex, node);
        return this;
    }
