    public boolean isNew() {
        return !isRoot() && !parent.base().hasChildNode(name) && parent.hasChildNode(name);
    }
