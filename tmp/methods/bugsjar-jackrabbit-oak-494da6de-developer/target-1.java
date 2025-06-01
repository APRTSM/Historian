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
            return newValidator(tree, null, provider);
        }
    }
    private static Validator newValidator(Tree parentBefore,
                                          Tree parentAfter,
                                          UserValidatorProvider provider) {
        return new VisibleValidator(
                new UserValidator(parentBefore, parentAfter, provider),
                true,
                true);
    }
    public Validator childNodeAdded(String name, NodeState after) throws CommitFailedException {
        Tree tree = checkNotNull(parentAfter.getChild(name));

        validateAuthorizable(tree, UserUtil.getType(tree));
        return newValidator(null, tree, provider);
    }
    public Validator childNodeChanged(String name, NodeState before, NodeState after) throws CommitFailedException {
        return newValidator(parentBefore.getChild(name),
                parentAfter.getChild(name), provider);
    }
