    protected boolean isNew() {
        return nodeBuilder.isNew();
    }
    NodeState rebase() {
        purge();
        branch.rebase();
        NodeState head = branch.getHead();
        reset(branch.getBase());
        super.reset(head);
        return head;
    }
    public boolean isNew() {
        return exists() && !getBaseState().exists();
    }
    public NodeState getBaseState() {
        if (base == null) {
            base = getParent().getBaseState().getChildNode(getName());
        }
        return base;
    }
    NodeState rebase() {
        purge();
        branch.rebase();
        NodeState head = branch.getHead();
        reset(branch.getBase());
        super.reset(head);
        return head;
    }
