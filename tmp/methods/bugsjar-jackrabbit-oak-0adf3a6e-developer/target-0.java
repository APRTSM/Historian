    UserValidator(Tree parentBefore, Tree parentAfter, UserValidatorProvider provider) {
        this.parentBefore = parentBefore;
        this.parentAfter = parentAfter;
        this.provider = provider;

        authorizableType = (parentAfter == null) ? null : UserUtil.getType(parentAfter);
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
