    private NodeState getBase() {
        if (parent == null) {
            return root.getBaseState();
        } else {
            return parent.getBase().getChildNode(name);
        }
    }
    protected boolean isNew() {
        return !getBase().exists();
    }
