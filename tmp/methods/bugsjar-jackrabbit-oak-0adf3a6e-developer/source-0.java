    public Validator childNodeDeleted(String name, NodeState before) throws CommitFailedException {
        Tree node = parentBefore.getChild(name);
        if (isAdminUser(node)) {
            String msg = "The admin user cannot be removed.";
            throw constraintViolation(27, msg);
        }
        return null;
    }
    UserValidator(Tree parentBefore, Tree parentAfter, UserValidatorProvider provider) {
        this.parentBefore = parentBefore;
        this.parentAfter = parentAfter;
        this.provider = provider;

        authorizableType = UserUtil.getType(parentAfter);
    }
