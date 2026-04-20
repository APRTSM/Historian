    public Validator childNodeAdded(String name, NodeState after) throws CommitFailedException {
        Tree treeAfter = checkNotNull(parentAfter.getChild(name));

        checkValidTree(parentAfter, treeAfter, after);
        return new AccessControlValidator(this, treeAfter);
    }
    public Validator childNodeChanged(String name, NodeState before, NodeState after) throws CommitFailedException {
        Tree treeAfter = checkNotNull(parentAfter.getChild(name));

        checkValidTree(parentAfter, treeAfter, after);
        return new AccessControlValidator(this, treeAfter);
    }
