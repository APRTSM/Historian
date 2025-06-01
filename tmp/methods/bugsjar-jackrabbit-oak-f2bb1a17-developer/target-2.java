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
