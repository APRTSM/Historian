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
    NodeState rebase() {
        purge();
        branch.rebase();
        NodeState head = branch.getHead();
        reset(head);
        return head;
    }
