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
    public Validator childNodeChanged(String name, NodeState before, NodeState after) throws CommitFailedException {
        return new UserValidator(parentBefore.getChild(name), parentAfter.getChild(name), provider);
    }
    public Validator childNodeAdded(String name, NodeState after) throws CommitFailedException {
        Tree tree = checkNotNull(parentAfter.getChild(name));

        validateAuthorizable(tree, UserUtil.getType(tree));
        return new VisibleValidator(new UserValidator(null, tree, provider), true, true);
    }
    public Validator childNodeDeleted(String name, NodeState before) throws CommitFailedException {
        Tree tree = parentBefore.getChild(name);
        AuthorizableType type = UserUtil.getType(tree);
        if (type == AuthorizableType.USER || type == AuthorizableType.GROUP) {
            if (isAdminUser(tree)) {
                String msg = "The admin user cannot be removed.";
                throw constraintViolation(27, msg);
            }
            return null;
        } else {
            return new VisibleValidator(new UserValidator(tree, null, provider), true, true);
        }
    }
