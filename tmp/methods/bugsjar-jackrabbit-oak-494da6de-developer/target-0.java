    public Validator childNodeChanged(String name, NodeState before, NodeState after) throws CommitFailedException {
        Tree treeAfter = checkNotNull(parentAfter.getChild(name));

        checkValidTree(parentAfter, treeAfter, after);
        return newValidator(this, treeAfter);
    }
    private static Validator newValidator(AccessControlValidator parent,
                                          Tree parentAfter) {
        return new VisibleValidator(
                new AccessControlValidator(parent, parentAfter),
                true,
                true);
    }
    public Validator childNodeAdded(String name, NodeState after) throws CommitFailedException {
        Tree treeAfter = checkNotNull(parentAfter.getChild(name));

        checkValidTree(parentAfter, treeAfter, after);
        return newValidator(this, treeAfter);
    }
