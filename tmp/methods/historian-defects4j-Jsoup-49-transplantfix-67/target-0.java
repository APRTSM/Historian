    protected void addChildren(int index, Node... children) {
        if (parent() != null) {
parent().addChildren(siblingIndex()+1, children);
}

Validate.noNullElements(children);
        ensureChildNodes();
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
        }
        reindexChildren(index);
    }
